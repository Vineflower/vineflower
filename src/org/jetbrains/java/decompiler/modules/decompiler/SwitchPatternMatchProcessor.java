package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public final class SwitchPatternMatchProcessor {
  public static boolean processPatternMatching(Statement root) {
    boolean ret = processPatternMatchingRec(root, root);

    if (ret) {
      SequenceHelper.condenseSequences(root);
    }

    return ret;
  }

  private static boolean processPatternMatchingRec(Statement stat, Statement root) {
    boolean ret = false;
    for (Statement st : new ArrayList<>(stat.getStats())) {
      ret |= processPatternMatchingRec(st, root);
    }

    if (stat instanceof SwitchStatement) {
      ret |= processStatement((SwitchStatement) stat, root);
    }

    return ret;
  }

  private static boolean processStatement(SwitchStatement stat, Statement root) {
    if (stat.isPhantom()) {
      return false;
    }

    SwitchHeadExprent head = (SwitchHeadExprent)stat.getHeadexprent();

    boolean switchPatternMatch = isSwitchPatternMatch(head);

    if (!switchPatternMatch) {
      return false;
    }

    // Found switch pattern match, start applying basic transformations
    // replace `SwitchBootstraps.typeSwitch<...>(o, idx)` with `o` if `idx` is not still used
    // if `idx` is still used, we have guarded labels
    InvocationExprent value = (InvocationExprent) head.getValue();
    List<Exprent> origParams = value.getLstParameters();
    boolean guarded = true;
    List<Pair<Statement, Exprent>> references = new ArrayList<>();
    if (origParams.get(1) instanceof VarExprent) {
      VarExprent var = (VarExprent) origParams.get(1);
      SwitchHelper.findExprents(root, Exprent.class, var::isVarReferenced, false, (st, expr) -> references.add(Pair.of(st, expr)));
      // If we have one reference...
      if (references.size() == 1) {
        // ...and its just assignment...
        Pair<Statement, Exprent> ref = references.get(0);
        if (ref.b instanceof AssignmentExprent) { // NOTE TO SELF: might break test??
          // ...remove the variable
          ref.a.getExprents().remove(ref.b);
          guarded = false;
        }
      }
    }

    Map<List<Exprent>, Exprent> guards = new HashMap<>(0);
    if (guarded) {
      guards = new HashMap<>(references.size());
      // in j17,
      // a guard takes the form of exactly
      // if (!guardCond) { idx = __thisIdx + 1; break; }
      // at the start of that branch
      // remove the initial assignment to 0
      Pair<Statement, Exprent> refA = references.get(0);
      if (refA.b instanceof AssignmentExprent && ((AssignmentExprent) refA.b).getRight() instanceof ConstExprent) {
        ConstExprent constExprent = (ConstExprent) ((AssignmentExprent) refA.b).getRight();
        if (constExprent.getConstType().typeFamily == CodeConstants.TYPE_FAMILY_INTEGER && constExprent.getIntValue() == 0) {
          refA.a.getExprents().remove(refA.b);
          references.remove(0);
        }
      }
      // TODO: more tests
      for (Pair<Statement, Exprent> reference : references) {
        if (reference.b instanceof AssignmentExprent) {
          Statement assignStat = reference.a;
          // check if the assignment follows the guard layout
          Statement parent = assignStat.getParent();
          // sometimes the assignment is not contained in the `if`, it's already inverted [TestSwitchPatternMatchingInstanceof]
          boolean invert = true;
          if (parent instanceof SequenceStatement && parent.getStats().size() == 2 && parent.getStats().get(1) == assignStat) {
            parent = parent.getStats().get(0);
            invert = false;
          }
          if (assignStat instanceof BasicBlockStatement
              && assignStat.getExprents().size() == 1
              && parent instanceof IfStatement
              && parent.getParent() instanceof SequenceStatement
              && parent.getParent().getParent() == stat) {
            Statement next = assignStat.getSuccessorEdges(StatEdge.TYPE_CONTINUE).get(0).getDestination();
            if (next == stat.getParent()) {
              IfStatement guardIf = (IfStatement) parent;
              Exprent guardExprent = guardIf.getHeadexprent().getCondition();
              List<Statement> caseStatements = stat.getCaseStatements();
              for (int i = 0; i < caseStatements.size(); i++) {
                if (caseStatements.get(i).containsStatement(reference.a)) {
                  // TODO: test if the cast is still inside the if (in inverted case) when the variable is used outside
                  List<Exprent> castExprent = Collections.singletonList(guardIf.getStats().get(0).getExprents().get(0));
                  if (invert) {
                    guardExprent = new FunctionExprent(FunctionExprent.FunctionType.BOOL_NOT, guardExprent, guardExprent.bytecode);
                  } else {
                    castExprent = parent.getStats().stream().flatMap(x -> x.getExprents().stream()).collect(Collectors.toList());
                    assignStat.replaceWithEmpty(); // normally removed in guardIf.replaceWithEmpty()
                  }
                  guards.put(stat.getCaseValues().get(i), guardExprent);
                  guardIf.replaceWithEmpty();
                  guardIf.getParent().getStats().remove(0);
                  Statement nextStat = guardIf.getParent().getStats().get(0);
                  if (nextStat instanceof BasicBlockStatement) {
                    nextStat.getExprents().addAll(0, castExprent);
                  } else {
                    nextStat.getFirst().getExprents().addAll(0, castExprent);
                  }
                  break;
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < stat.getCaseStatements().size(); i++) {
      Statement caseStat = stat.getCaseStatements().get(i);

      List<Exprent> allCases = stat.getCaseValues().get(i);
      Exprent caseExpr = allCases.get(0);

      // Default branch
      if (caseExpr == null) {
        continue;
      }

      if (guards.containsKey(allCases)) {
        // TODO: this is bad
        while(stat.getCaseGuards().size() <= i)
          stat.getCaseGuards().add(null);
        stat.getCaseGuards().set(i, guards.get(allCases));
      }
      if (caseExpr instanceof ConstExprent) {
        int caseValue = ((ConstExprent)caseExpr).getIntValue();

        if (caseValue == -1) {
          allCases.remove(caseExpr);
          ConstExprent nullConst = new ConstExprent(VarType.VARTYPE_NULL, null, null);
          if (allCases.contains(null)) {
            allCases.add(allCases.indexOf(null), nullConst);
          } else {
            allCases.add(nullConst);
          }
        }
      }

      if (caseStat instanceof SequenceStatement) {
        Statement oldStat = caseStat;
        caseStat = caseStat.getStats().get(0);
        if (oldStat.getStats().size() == 1) {
          oldStat.replaceWith(caseStat);
        }
      }
      if (!(caseStat instanceof BasicBlockStatement)) {
        caseStat = caseStat.getFirst();
      }
      // Make instanceof
      BasicBlockStatement caseStatBlock = (BasicBlockStatement)caseStat;
      if (caseStatBlock.getExprents().size() >= 1) {
        Exprent expr = caseStatBlock.getExprents().get(0);
        if (expr instanceof AssignmentExprent) {
          AssignmentExprent assign = (AssignmentExprent)expr;

          if (assign.getLeft() instanceof VarExprent) {
            VarExprent var = (VarExprent)assign.getLeft();

            if (assign.getRight() instanceof FunctionExprent && ((FunctionExprent)assign.getRight()).getFuncType() == FunctionExprent.FunctionType.CAST) {
              FunctionExprent cast = (FunctionExprent)assign.getRight();

              List<Exprent> operands = new ArrayList<>();
              operands.add(cast.getLstOperands().get(0)); // checking var
              operands.add(cast.getLstOperands().get(1)); // type
              operands.add(var); // pattern match var

              FunctionExprent func = new FunctionExprent(FunctionExprent.FunctionType.INSTANCEOF, operands, null);

              caseStatBlock.getExprents().remove(0);

              // TODO: ssau representation
              allCases.set(0, func);
            }
          }
        }
      }
    }

    List<StatEdge> sucs = stat.getSuccessorEdges(StatEdge.TYPE_REGULAR);

    if (!sucs.isEmpty()) {

      Statement suc = sucs.get(0).getDestination();
      if (!(suc instanceof BasicBlockStatement)) { // make basic block if it isn't found
        Statement oldSuc = suc;

        suc = BasicBlockStatement.create();
        SequenceStatement seq = new SequenceStatement(stat, suc);

        seq.setParent(stat.getParent());

        stat.replaceWith(seq);

        seq.setAllParent();

        // Replace successors with the new basic block
        for (Statement st : stat.getCaseStatements()) {
          for (StatEdge edge : st.getAllSuccessorEdges()) {
            if (edge.getDestination() == oldSuc) {
              st.removeSuccessor(edge);

              st.addSuccessor(new StatEdge(edge.getType(), st, suc, seq));
            }
          }
        }

        // Control flow from new basic block to the next one
        suc.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, suc, oldSuc, seq));
      }

      stat.setPhantom(true);
      suc.getExprents().add(0, new SwitchExprent(stat, VarType.VARTYPE_INT, false, true));
    }

    head.setValue(origParams.get(0));

    if (guarded && stat.getParent() instanceof DoStatement) {
      // remove the enclosing while(true) loop of a guarded switch
      stat.getParent().replaceWith(stat);
      for (StatEdge edge : stat.getPredecessorEdges(StatEdge.TYPE_CONTINUE)) {
        stat.removePredecessor(edge);
        edge.getSource().removeSuccessor(edge);
      }
    }

    return false;
  }

  private static boolean isSwitchPatternMatch(SwitchHeadExprent head) {
    Exprent value = head.getValue();

    if (value instanceof InvocationExprent) {
      InvocationExprent invoc = (InvocationExprent)value;

      return invoc.getInvocationType() == InvocationExprent.InvocationType.DYNAMIC && invoc.getName().equals("typeSwitch");
    }

    return false;
  }
}
