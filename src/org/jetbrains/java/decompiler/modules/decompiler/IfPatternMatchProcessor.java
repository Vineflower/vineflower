package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.util.VBStyleCollection;

import java.util.List;

/**
 * Handles pattern matching for instanceof in statements.
 *
 * @author SuperCoder79 and Kroppeb
 */
public final class IfPatternMatchProcessor {
  public static boolean matchInstanceof(RootStatement root) {
    return matchInstanceofRec(root, root, null);
  }

  private static boolean matchInstanceofRec(Statement statement, RootStatement root, Statement next) {
    boolean res = false;
    if (statement.type == Statement.TYPE_SEQUENCE) {
      VBStyleCollection<Statement, Integer> stats = statement.getStats();
      for (int i = 0; i < stats.size(); i++) {
        Statement stat = stats.get(i);
        if (matchInstanceofRec(stat, root, i + 1 < stats.size() ? stats.get(i + 1) : null)) {
          res = true;
        }
      }
    } else {
      for (Statement stat : statement.getStats()) {
        if (matchInstanceofRec(stat, root, null)) {
          res = true;
        }
      }
    }

    if (statement instanceof IfStatement) {
      res |= handleIf((IfStatement) statement, root, next);
    }

    return res;
  }

  private static boolean handleIf(IfStatement statement, RootStatement root, Statement next) {
    Exprent condition = statement.getHeadexprent().getCondition();

    if (condition.type != Exprent.EXPRENT_FUNCTION) {
      return false;
    }

    FunctionExprent func = (FunctionExprent) condition;

    boolean inverted;
    if (func.getFuncType() != FunctionExprent.FUNCTION_EQ && func.getFuncType() != FunctionExprent.FUNCTION_NE) {
      // handle double negation, this usually means that the if statement has no sub-statements, and we
      // will need to use next as a target
      if (func.getFuncType() == FunctionExprent.FUNCTION_BOOL_NOT) {
        func = (FunctionExprent) func.getLstOperands().get(0);
        if (func.getFuncType() != FunctionExprent.FUNCTION_EQ && func.getFuncType() != FunctionExprent.FUNCTION_NE) {
          return false;
        } else {
          inverted = func.getFuncType() == FunctionExprent.FUNCTION_NE;
        }
      } else {
        return false;
      }
    } else {
      inverted = func.getFuncType() != FunctionExprent.FUNCTION_NE;
    }


    List<Exprent> exprents = func.getLstOperands();
    Exprent l = exprents.get(0);
    Exprent r = exprents.get(1);


    if (l.type != Exprent.EXPRENT_FUNCTION ||
        r.type != Exprent.EXPRENT_CONST ||
        ((ConstExprent) r).getIntValue() != 0) {
      return false;
    }

    FunctionExprent iof = (FunctionExprent) l;

    // Check for instanceof
    if (iof.getFuncType() != FunctionExprent.FUNCTION_INSTANCEOF) {
      return false;
    }

    Exprent source = iof.getLstOperands().get(0);
    Exprent target = iof.getLstOperands().get(1);

    Statement targetStat = inverted ? statement.getElsestat() : statement.getIfstat();

    if (statement.getElsestat() == null && statement.getIfstat() == null) {
      // if statement has no sub-statements, and we will need to see if we can use next as a target
      if (next == null) {
        return false;
      }

      // Check if the target branch is an edge, if so, we can't use it as a target
      if (inverted ? statement.getElseEdge() != null : statement.getIfEdge() != null) {
        return false;
      }

      // Check if the non-target branch is an edge, if so, we should be able to use next as a target
      if (inverted ? statement.getIfEdge() != null : statement.getElseEdge() != null) {
        targetStat = next;
      } else {
        return false;
      }
    }

    while (true) {
      if (targetStat == null) {
        return false;
      } else if (targetStat.getExprents() == null || targetStat.getExprents().isEmpty()) {
        targetStat = targetStat.getFirst();
      } else {
        break;
      }
    }

    Exprent first = targetStat.getExprents().get(0);

    // Check inside the if statement for a cast
    if (first.type != Exprent.EXPRENT_ASSIGNMENT) {
      return false;
    }

    // If it's an assignment, get both sides
    Exprent left = first.getAllExprents().get(0);
    Exprent right = first.getAllExprents().get(1);


    // Right side needs to be a cast function
    if (right.type != Exprent.EXPRENT_FUNCTION ||
        ((FunctionExprent) right).getFuncType() != FunctionExprent.FUNCTION_CAST) {
      return false;
    }

    Exprent casted = right.getAllExprents().get(0);
    Exprent castedType = right.getAllExprents().get(1);

    // Check if the exprent being cast is the exprent on the left side of the instanceof
    // Make sure the left-hand side is a variable and the cast matches the instanceof
    if (!source.equals(casted) || left.type != Exprent.EXPRENT_VAR || !target.equals(castedType)) {
      return false;
    }

    // List<VarVersionPair> vvs = new ArrayList<>();

    // We need to make sure we're not assigning to previously assigned variables.
    // This gets all predecessors of the if statement and gathers all the variable assignments inside.
    // TODO: cache this
    // findVarsInPredecessors(vvs, statement.getIfstat());

    // VarVersionPair var = ((VarExprent) left).getVarVersionPair();

    // Stop processing if this variable has already been seen
    // for (VarVersionPair vv : vvs) {
    //   if (var.var == vv.var) {
    //     return false;
    //   }
    // }

    // Add the exprent to the instanceof exprent and remove it from the inside of the if statement
    iof.getLstOperands().add(2, left);
    targetStat.getExprents().remove(0);


    if (targetStat.getExprents().isEmpty()) {
      // targetStat.getSuccessorEdges(Statement.)
      IfHelper.fixIf(statement, next);
    }

    return true;
  }

  // Finds all assignments and their associated variables in a statement's predecessors.
  private static void findVarsInPredecessors(List<VarVersionPair> vvs, Statement root) {
    for (StatEdge pred : root.getAllPredecessorEdges()) {
      Statement stat = pred.getSource();

      if (stat.getExprents() != null) {
        for (Exprent exprent : stat.getExprents()) {

          // Check for assignment exprents
          if (exprent.type == Exprent.EXPRENT_ASSIGNMENT) {
            AssignmentExprent assignment = (AssignmentExprent) exprent;

            // If the left type of the assignment is a variable, store it's var info
            if (assignment.getLeft().type == Exprent.EXPRENT_VAR) {
              vvs.add(((VarExprent) assignment.getLeft()).getVarVersionPair());
            }
          }
        }
      }
    }
  }
}
