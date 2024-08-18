package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructRecordComponent;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.*;

public final class IfPatternMatchProcessor {
  public static boolean matchInstanceof(RootStatement root) {
    boolean res = matchInstanceofRec(root, root);

    if (res) {
      ValidationHelper.validateStatement(root);

      SequenceHelper.condenseSequences(root);

      // IfHelper already called SequenceHelper.condenseSequences if it returned true
      if (IfHelper.mergeAllIfs(root)) {
        improvePatternTypes(root);
      } else {
        SequenceHelper.condenseSequences(root);
      }
    }

    return res;
  }

  private static boolean matchInstanceofRec(Statement statement, RootStatement root) {
    boolean res = false;
    for (Statement stat : statement.getStats()) {
      if (matchInstanceofRec(stat, root)) {
        res = true;
      }
    }

    if (statement instanceof IfStatement) {
      res |= handleIf((IfStatement) statement, root);
    }

    return res;
  }

  private static boolean handleIf(IfStatement statement, RootStatement root) {
    Exprent condition = statement.getHeadexprent().getCondition();


    Exprent lastIfTrue = getLastExprentWhen(condition, true, true);
    Exprent lastIfFalse = getLastExprentWhen(condition, false, true);


    boolean updated = false;
    if (lastIfTrue != null) {
      if(checkBranch(lastIfTrue, statement, statement.getIfEdge().getDestination(), root)) {
        updated = true;

        // The if branch might be empty now
        statement.fixIfInvariantEmptyIfBranch();
      }
    }

    if (!updated && lastIfFalse != null) {
      if (statement.getElseEdge() != null) {
        if(checkBranch(lastIfFalse, statement, statement.getElseEdge().getDestination(), root)) {
          updated = true;

          // The else branch might be empty now
          statement.fixIfInvariantEmptyElseBranch();
        }
      } else {
        var allSuc = statement.getAllSuccessorEdges();
        if (allSuc.size() == 1) {
          // In theory, the if branch can 'fall through' to here, but then this branch has multiple predecessors
          // and will get left alone anyway
          if(checkBranch(lastIfFalse, statement, allSuc.get(0).getDestination(), root)) {
            updated = true;

            // No need to fix 'if' invariants
          }
        }
      }
    }

    return updated;
  }

  private static boolean checkBranch(Exprent exprent, IfStatement statement, Statement branch, RootStatement root) {
    if (!(exprent instanceof FunctionExprent) || branch.getAllPredecessorEdges().size() != 1) {
      // We can only inline into 'instanceof', and only if the target branch doesn't have multiple predecessors
      // TODO: make checking for multiple predecessors less expensive
      return false;
    }

    FunctionExprent iof = (FunctionExprent) exprent;

    // Check for instanceof and isn't a pattern match yet
    if (iof.getFuncType() != FunctionType.INSTANCEOF || iof.getLstOperands().size() != 2) {
      return false;
    }

    Exprent source = iof.getLstOperands().get(0);
    if ((source.getExprentUse() & Exprent.MULTIPLE_USES) == 0) {
      return false;
    }

    Exprent target = iof.getLstOperands().get(1);

    Statement head = branch.getBasichead();

    if (head.getExprents() == null || head.getExprents().isEmpty()) {
      return false;
    }

    Exprent first = head.getExprents().get(0);

    // Check inside of the if statement for a cast
    if (!(first instanceof AssignmentExprent)) {
      return false;
    }

    // If it's an assignement, get both sides
    Exprent left = first.getAllExprents().get(0);
    Exprent right = first.getAllExprents().get(1);

    boolean result = findPatternMatchingInstanceof(left, right, source, target, branch, iof, head);

    if (head.getExprents() != null && !head.getExprents().isEmpty() && head.getExprents().get(0) instanceof AssignmentExprent assignment) {
      // If it's an assignement, get both sides
      left = assignment.getAllExprents().get(0);
      right = assignment.getAllExprents().get(1);

      // Right side needs to be a cast function
      // If it's not, we might be a record pattern match
      if (!(right instanceof FunctionExprent)) {
        result |= identifyRecordPatternMatch(statement, branch, iof, assignment);
      }
    }

    statement.setPatternMatched(true);

    BasicBlockStatement before = statement.getBasichead();
    if (before.getExprents() != null && before.getExprents().size() > 0) {
      Exprent last = before.getExprents().get(before.getExprents().size() - 1);
      if (last instanceof AssignmentExprent && source instanceof VarExprent) {
        Exprent stored = last.getAllExprents().get(0);
        Exprent method = last.getAllExprents().get(1);
        VarExprent checked = (VarExprent) source;
        if ((!(method instanceof FunctionExprent) || ((FunctionExprent) method).getFuncType() != FunctionType.CAST)
            && checked.equals(stored) && !checked.isVarReferenced(root, (VarExprent) stored)) {
          iof.getLstOperands().set(0, last.getAllExprents().get(1));
          before.getExprents().remove(before.getExprents().size() - 1);
        }
      }
    }

    return result;
  }

  private static boolean findPatternMatchingInstanceof(Exprent left, Exprent right, Exprent source, Exprent target, Statement branch, FunctionExprent iof, Statement head) {
    if (!(right instanceof FunctionExprent function) || function.getFuncType() != FunctionType.CAST) {
      return false;
    }

    Exprent casted = right.getAllExprents().get(0);

    // Check if the exprent being casted is the exprent on the left side of the instanceof
    if (!source.equals(casted)) {
      return false;
    }

    // Make sure the left hand side is a variable and it's type matches the target of the cast
    if (!(left instanceof VarExprent) || !target.getExprType().equals(left.getExprType())) {
      return false;
    }

    List<VarVersionPair> vvs = new ArrayList<>();

    // We need to make sure we're not assigning to previously assigned variables.
    // This gets all predecessors of the if statement and gathers all the variable assignments inside.
    // TODO: cache this
    findVarsInPredecessors(vvs, branch);

    VarVersionPair var = ((VarExprent) left).getVarVersionPair();

    // Stop processing if this variable has already been seen
    for (VarVersionPair vv : vvs) {
      if (var.var == vv.var) {
        return false;
      }
    }
    
    VarType storeType = left.getInferredExprType(null);

    // Add the exprent to the instanceof exprent and remove it from the inside of the if statement
    iof.getLstOperands().add(2, left);
    head.getExprents().remove(0);
    if (storeType.isGeneric()) {
      iof.getLstOperands().set(1, new ConstExprent(storeType, null, iof.getLstOperands().get(1).bytecode));
    }
    return true;
  }

  // Finds all assignments and their associated variables in a statement's predecessors.
  private static void findVarsInPredecessors(List<VarVersionPair> vvs, Statement root) {
    Deque<Statement> stack = new ArrayDeque<>();
    Set<Statement> seen = new HashSet<>();

    stack.add(root);

    while (!stack.isEmpty()) {
      Statement st = stack.pop();
      if (!seen.add(st)) {
        continue;
      }

      if (st.getParent() instanceof IfStatement || st instanceof IfStatement) {
        stack.add(st.getParent());
      }

      for (StatEdge pred : st.getAllPredecessorEdges()) {
        Statement stat = pred.getSource();
        stack.add(stat);
        if (stat == root) {
          continue;
        }

        if (stat.getExprents() != null) {
          for (Exprent exprent : stat.getExprents()) {

            // Check for assignment exprents
            if (exprent instanceof AssignmentExprent) {
              AssignmentExprent assignment = (AssignmentExprent) exprent;

              // If the left type of the assignment is a variable, store it's var info
              if (assignment.getLeft() instanceof VarExprent) {
                vvs.add(((VarExprent) assignment.getLeft()).getVarVersionPair());
              }
            }
          }
        }
      }
    }
  }

  private static boolean identifyRecordPatternMatch(IfStatement stat, Statement branch, FunctionExprent instOf, AssignmentExprent head) {
    if (!stat.getTopParent().mt.getBytecodeVersion().hasRecordPatternMatching()) {
      return false;
    }

    Exprent headLeft = head.getLeft();
    Exprent headRight = head.getRight();

    // Check for:
    //
    // if (v instanceof MyType) {
    //   var10000 = v;
    // ...
    if (!(instOf.getLstOperands().size() > 2 ? instOf.getLstOperands().get(2) : instOf.getLstOperands().get(0)).equals(headRight)) {
      return false;
    }

    Statement original = branch;

    VarType type = instOf.getLstOperands().get(1).getExprType();

    StructClass cl = DecompilerContext.getStructContext().getClass(type.value);

    // Iteratively go through the sequence to see if it extracts from the record

    // The general strategy is to identify an "extracting try" [1] for each record component.
    // If we identify it, continue matching. Between each try we might see pseudo stack ops [2]
    // that we'll want to clean up as well. If all the components were matched, then we are able
    // to create the pattern with the variables.
    //
    // [1]:
    // try {
    //   exVar = <stackVar>.<component>();
    // } catch (Throwable t) {
    //   throw new MatchException(...);
    // }
    //
    // [2]:
    // realVar = exVar;
    // <stackVar> = <originalVar>;

    // Ending exprents we may want to remove
    Map<BasicBlockStatement, Exprent> remove = new HashMap<>();
    // Statements that ought to be destroyed as a result of creating the pattern
    List<Statement> toDestroy = new ArrayList<>();

    PatternData pattern = getChildPattern(cl, headRight, type, branch, 1, toDestroy, remove);
    if (pattern == null) {
      return false;
    }
    branch = pattern.stat;
    if (instOf.getLstOperands().size() > 2) {
      instOf.getLstOperands().set(2, pattern.exp);
    } else {
      instOf.getLstOperands().add(2, pattern.exp);
    }
    stat.setPatternMatched(true);

    if (original != branch) {
      stat.replaceStatement(original, branch);
    }

    for (Statement st : toDestroy) {
      st.replaceWithEmpty();
    }

    for (Map.Entry<BasicBlockStatement, Exprent> e : remove.entrySet()) {
      e.getKey().getExprents().remove(e.getValue());
    }

    return true;
  }

  private static PatternData getChildPattern(StructClass cl, Exprent storeVariable, VarType type, Statement branch, int stIdx, List<Statement> toDestroy, Map<BasicBlockStatement, Exprent> remove) {
    if (cl == null || cl.getRecordComponents() == null) {
      return null; // No idea what class, or not a record!
    }

    record PatternStore(StructRecordComponent component, StructClass cl, VarType type, VarExprent store) {
    }
    List<PatternStore> patternStores = new ArrayList<>();
    List<StructRecordComponent> comp = cl.getRecordComponents();

    // Map which variable refers to which part of the record
    Map<StructRecordComponent, Exprent> vars = new LinkedHashMap<>();
    for (StructRecordComponent c : comp) {
      if (branch.getStats().size() <= stIdx) {
        return null;
      }

      Statement next = branch.getStats().get(stIdx);
      if (next instanceof CatchStatement catchSt && catchSt.getVars().size() == 1 && catchSt.getVars().get(0).getVarType().value.equals("java/lang/Throwable")) {
        // Check catch for "throw new MatchException"
        VarExprent foundVar = null;
        if (catchSt.getStats().size() == 2 && isStatementMatchThrow(catchSt.getStats().get(1))) {
          // Now make sure the inside of the try is ok
          Statement inner = catchSt.getStats().get(0);
          if (inner instanceof BasicBlockStatement) {
            // var<x> = var10000.<comp>()
            if (inner.getExprents().size() == 1 && inner.getExprents().get(0) instanceof AssignmentExprent assign) {
              // Make sure the invocation matches the record component
              if (assign.getLeft() instanceof VarExprent var && assign.getRight() instanceof InvocationExprent invok && invok.getClassname().equals(type.value)) {
                if (invok.getName().equals(c.getName())) {
                  // Found one!
                  foundVar = var;
                }
              }
            }
          }
        }

        if (foundVar == null) {
          return null;
        }

        toDestroy.add(next);

        // Check the next statement for any pseudo stack ops
        stIdx++;
        if (branch.getStats().size() > stIdx) {
          next = branch.getStats().get(stIdx);

          boolean ok = false;
          if (next instanceof BasicBlockStatement bb && next.getExprents().size() > 0) {
            // look for "realVar = exVar;" to remove it
            if (next.getExprents().get(0) instanceof AssignmentExprent assign && assign.getLeft() instanceof VarExprent var) {
              if (assign.getRight().equals(foundVar)) {
                vars.put(c, var);

                ok = true;

                // Check for "<stackVar> = <originalVar>;"
                // If that's the only other thing in the statement, then we can destroy it!
                boolean destroyed = false;
                if (next.getExprents().size() == 2) {
                  if (next.getExprents().get(1) instanceof AssignmentExprent nAssign && nAssign.getRight().equals(storeVariable)) {
                    toDestroy.add(next);

                    destroyed = true;
                  }
                }

                // If we haven't destroyed it, we should remove the "realVar = exVar;" anyway. Mark it as such.
                if (!destroyed) {
                  remove.put(bb, assign);
                }
              }
            }
          } else {
            // Is the next statement an if with an instanceof inside? It might be a type-improving if. Search inside it too.
            if (next instanceof IfStatement ifSt && ifSt.iftype == IfStatement.IFTYPE_IF
              && ifSt.getHeadexprent().getCondition() instanceof FunctionExprent func && func.getFuncType() == FunctionType.INSTANCEOF) {

              // "<stackVar> = <originalVar>;" idiom
              // Ensure this is the right idiom be fore we mark it for destruction.
              if (branch.getBasichead().getExprents().size() == 1) {
                if (branch.getBasichead().getExprents().get(0) instanceof AssignmentExprent assign
                  && assign.getLeft() instanceof VarExprent && assign.getRight() instanceof VarExprent) {
                  toDestroy.add(branch.getBasichead());
                }
              }

              Exprent store = func.getLstOperands().size() > 2 ? func.getLstOperands().get(2) : func.getLstOperands().get(0);
              if (store instanceof VarExprent variable) {
                patternStores.add(new PatternStore(c, DecompilerContext.getStructContext().getClass(variable.getExprType().value), variable.getExprType(), variable));
                vars.put(c, variable);
                ok = true;
              }

              branch = ifSt.getIfstat();
              stIdx = 0;
            }
          }

          // If we found a "realVar = exVar;" then we can skip over this statement and move on.
          // Otherwise, "exVar" is probably the real var. Mark it as such.
          if (!ok) {
            vars.put(c, foundVar);
          }

          stIdx++;
        }
      } else {
        return null;
      }
    }

    for (PatternStore patternStore : patternStores) {
      List<Statement> tmpToDestroy = new ArrayList<>();
      Map<BasicBlockStatement, Exprent> tmpRemove = new HashMap<>();
      PatternData patternData = getChildPattern(patternStore.cl, patternStore.store, patternStore.type, branch, stIdx, tmpToDestroy, tmpRemove);
      if (patternData != null) {
        vars.put(patternStore.component, patternData.exp);
        branch = patternData.stat;
        stIdx = patternData.index;
        toDestroy.addAll(tmpToDestroy);
        remove.putAll(tmpRemove);
      }
    }

    PatternExprent pattern = new PatternExprent(PatternExprent.recordData(cl), type, new ArrayList<>(vars.values()));
    return new PatternData(pattern, branch, stIdx);
  }

  private record PatternData(PatternExprent exp, Statement stat, int index) {}

  public static boolean isStatementMatchThrow(Statement st) {
    if (st instanceof BasicBlockStatement && st.getExprents().size() == 1) {
      // throw ...
      if (st.getExprents().get(0) instanceof ExitExprent exit && exit.getExitType() == ExitExprent.Type.THROW) {
        // throw new ...
        if (exit.getValue() instanceof NewExprent newEx) {
          // throw new MatchException
          return newEx.getNewType().value.equals("java/lang/MatchException");
        }
      }
    }

    return false;
  }

  /**
   * Gets the last guaranteed executed exprent in an expression.
   * @param ifTrue if true, gets the last executed exprent when the condition is true.
   *               if false, gets the last executed exprent when the condition is false.
   * @param onlyIfTrue if true, only returns the last executed exprent if the exprent had to return true for
   *                  the requested outcome to be selected.
   * @return the last executed exprent
   */
  public static Exprent getLastExprentWhen(Exprent base, boolean ifTrue, boolean onlyIfTrue) {
    switch (base.type){
      case FUNCTION: {
        FunctionExprent func = (FunctionExprent) base;
        switch (func.getFuncType()) {
          case BOOLEAN_AND: {
            if (ifTrue) {
              // when `&&` returns true, the second exprent had to run and return true
              return getLastExprentWhen(func.getLstOperands().get(1), true, onlyIfTrue);
            }
            // when `&&` returns false, either could have returned false, so we go to
            // the default case of returning ourselves
            break;
          }
          case BOOLEAN_OR: {
            if (!ifTrue) {
              // when `||` returns false, the second exprent had to run and return false
              return getLastExprentWhen(func.getLstOperands().get(1), false, onlyIfTrue);
            }
            // when `||` returns true, either could have returned true, so we go to
            // the default case of returning ourselves
            break;
          }
          case BOOL_NOT: {
            // when `!` returns true, the exprent had to return false
            // when `!` returns false, the exprent had to return true
            return getLastExprentWhen(func.getLstOperands().get(0), !ifTrue, onlyIfTrue);
          }

          // TEMPORARY
          // This is here because things like `a instanceof B` are initially decompiled as
          // `(a instanceof B) != false`, and this is only cleaned up at the end by
          // secondaryFunctionsHelper
          case EQ: {
            Exprent rhs = func.getLstOperands().get(1);
            if (rhs.type == Exprent.Type.CONST) {
              ConstExprent constExprent = (ConstExprent) rhs;
              if (constExprent.getConstType() == VarType.VARTYPE_BOOLEAN) {
                if (constExprent.getIntValue() == 0) {
                  // `x == false` is the same as `!x`
                  return getLastExprentWhen(func.getLstOperands().get(0), !ifTrue, onlyIfTrue);
                } else {
                  // `x == true` is the same as `x`
                  return getLastExprentWhen(func.getLstOperands().get(0), ifTrue, onlyIfTrue);
                }
              }
            }
            break;
          }
          case NE: {
            Exprent rhs = func.getLstOperands().get(1);
            if (rhs.type == Exprent.Type.CONST) {
              ConstExprent constExprent = (ConstExprent) rhs;
              if (constExprent.getConstType() == VarType.VARTYPE_BOOLEAN) {
                if (constExprent.getIntValue() == 0) {
                  // `x != false` is the same as `x`
                  return getLastExprentWhen(func.getLstOperands().get(0), ifTrue, onlyIfTrue);
                } else {
                  // `x != true` is the same as `!x`
                  return getLastExprentWhen(func.getLstOperands().get(0), !ifTrue, onlyIfTrue);
                }
              }
            }
            break;
          }
        }
      }
    }

    // if we're only looking for exprents that had to return true, and this exprent didn't, return null
    if (onlyIfTrue && !ifTrue) {
      return null;
    }

    // otherwise, return ourselves
    return base;
  }

  private static boolean improvePatternTypes(Statement stat) {
    boolean res = false;
    for (Statement st : stat.getStats()) {
      res |= improvePatternTypes(st);
    }

    if (stat instanceof IfStatement ifSt) {
      Exprent cond = ifSt.getHeadexprent().getCondition();

      if (improvePatternType(ifSt.getHeadexprent(), cond, ifSt.getIfstat())) {
        res = true;
      }
    }

    return res;
  }

  private static boolean improvePatternType(Exprent parent, Exprent ex, Statement st) {
    boolean res = false;
    for (Exprent e : ex.getAllExprents(false, true)) {
      // don't recurse on self
      if (e != ex) {
        res |= improvePatternType(ex, e, st);
      }

      if (e instanceof FunctionExprent fn && fn.getFuncType() == FunctionType.BOOLEAN_AND) {
        Exprent base = fn.getLstOperands().get(0);

        // Check for record pattern instanceof
        if (base instanceof FunctionExprent baseFn && baseFn.getFuncType() == FunctionType.INSTANCEOF
          && baseFn.getLstOperands().size() > 2 && baseFn.getLstOperands().get(2) instanceof PatternExprent pattern
          && pattern.getData() instanceof PatternExprent.PatternData.RecordPatternData) {
          // Found one? now find type-enhancing instanceofs in the other arm

          // Map a list of vars 1:1 with the exprents in the pattern
          List<VarExprent> vars = new ArrayList<>();

          for (Exprent patternEx : pattern.getExprents()) {
            if (patternEx instanceof VarExprent var) {
              vars.add(var);
            } else {
              vars.add(null);
            }

            // TODO: recursively look?
          }

          for (int j = 0; j < vars.size(); j++) {
            VarExprent var = vars.get(j);
            if (var == null) {
              continue;
            }

            // Now go through the following ordeal to improve pattern types.
            // Look for cases that look like 'Rec(Object synth) && synth instanceof Type t' where 'synth' is a synthetic
            // variable that is only used in the pattern.

            // Is the 'synth' variable used outside the pattern? All hope is lost.
            if (var.isVarReferenced(st)) {
              continue;
            }

            // Go through all of the exprents one by one to see if we can find redundant instanceofs
            // We need to start at the parent, as in the case where there is only one instanceof, such as
            // 'o instanceof Rec(Object x) && x instanceof String s', we need to replace the whole expression with the
            // left hand side.
            out:
            for (Exprent exp : parent.getAllExprents(true, true)) {
              for (Exprent inst : exp.getAllExprents()) {
                if (inst instanceof FunctionExprent instFun && instFun.getFuncType() == FunctionType.BOOLEAN_AND) {
                  // Search each arm of the boolean and
                  for (int i = 0; i < 2; i++) {
                    Exprent inner = instFun.getLstOperands().get(i);
                    if (inner instanceof FunctionExprent innerFun && innerFun.getFuncType() == FunctionType.INSTANCEOF && innerFun.getLstOperands().size() > 2) {
                      if (innerFun.getLstOperands().get(0).equals(var)) {

                        // Replace the var
                        pattern.getExprents().set(j, innerFun.getLstOperands().get(2));
                        pattern.getVarTypes().set(j, innerFun.getLstOperands().get(1).getExprType());

                        // replace 'A && B' where A is the redundant instanceof with simply 'B'
                        exp.replaceExprent(inst, instFun.getLstOperands().get(i ^ 1));

                        break out;
                      }
                    }
                  }
                }
              }
            }

            // Iterate again, to try to replace all components
          }
        }
      }
    }

    return res;
  }
}
