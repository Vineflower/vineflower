package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles pattern matching for instanceof in statements.
 *
 * @author SuperCoder79
 */
public final class IfPatternMatchProcessor {
  public static boolean matchInstanceof(RootStatement root) {
    boolean res = matchInstanceofRec(root, root);

    if (res) {
      ValidationHelper.validateStatement(root);

      // IfHelper already called SequenceHelper.condenseSequences if it returned true
      if (!IfHelper.mergeAllIfs(root)) {
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
      if(checkBranch(lastIfTrue, statement, statement.getIfEdge().getDestination())) {
        updated = true;

        // The if branch might be empty now
        statement.fixIfInvariantEmptyIfBranch();
      }
    }

    if (!updated && lastIfFalse != null) {
      if (statement.getElseEdge() != null) {
        if(checkBranch(lastIfFalse, statement, statement.getElseEdge().getDestination())) {
          updated = true;

          // The else branch might be empty now
          statement.fixIfInvariantEmptyElseBranch();
        }
      } else {
        var allSuc = statement.getAllSuccessorEdges();
        if (allSuc.size() == 1) {
          // In theory, the if branch can 'fall through' to here, but then this branch has multiple predecessors
          // and will get left alone anyway
          if(checkBranch(lastIfFalse, statement, allSuc.get(0).getDestination())) {
            updated = true;

            // No need to fix 'if' invariants
          }
        }
      }
    }

    return updated;
  }

  private static boolean checkBranch(Exprent exprent, IfStatement statement, Statement branch) {
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
    Exprent target = iof.getLstOperands().get(1);

    Statement head = branch.getBasichead();

    if (head.getExprents() == null) {
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

    // Right side needs to be a cast function
    if (!(right instanceof FunctionExprent)) {
      return false;
    }

    if (((FunctionExprent) right).getFuncType() != FunctionType.CAST) {
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

    // Add the exprent to the instanceof exprent and remove it from the inside of the if statement
    iof.getLstOperands().add(2, left);
    head.getExprents().remove(0);

    statement.setPatternMatched(true);

    return true;
  }

  // Finds all assignments and their associated variables in a statement's predecessors.
  // FIXME: This isn't working as it should! it should be traversing the predecessor tree!
  private static void findVarsInPredecessors(List<VarVersionPair> vvs, Statement root) {
    for (StatEdge pred : root.getAllPredecessorEdges()) {
      Statement stat = pred.getSource();

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
          // (Nothing is as permanent as a temporary solution :p)
          // This is here because things like `a instanceof B` are initially decompiled as
          // `(a instanceof B) != false`, and this is only cleaned up at the end by
          // secondaryFunctionsHelper :/
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
}
