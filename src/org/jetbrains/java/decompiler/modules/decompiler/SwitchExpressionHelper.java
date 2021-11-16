package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SwitchExpressionHelper {
  public static boolean processSwitchExpressions(Statement stat) {
    boolean ret = false;
    for (Statement st : stat.getStats()) {
      ret |= processSwitchExpressions(st);
    }

    if (stat.type == Statement.TYPE_SWITCH) {
      ret |= processStatement((SwitchStatement) stat);
    }

    return ret;
  }

  private static boolean processStatement(SwitchStatement stat) {
    if (stat.isPhantom()) {
      return false;
    }

    // At this stage, there are no variable assignments
    // So we need to figure out which variable, if any, this switch statement is an expression of and make it generate.

    VarVersionPair foundVar = null;
    VarExprent found = null;
    for (Statement caseStat : stat.getCaseStatements()) {
      // Need to be basic blocks for now
      if (caseStat.type != Statement.TYPE_BASICBLOCK) {
        return false;
      }

      List<Exprent> exprents = caseStat.getExprents();
      if (exprents != null && !exprents.isEmpty()) {
        Exprent exprent = exprents.get(exprents.size() - 1);

        if (exprent.type == Exprent.EXPRENT_ASSIGNMENT && ((AssignmentExprent)exprent).getLeft().type == Exprent.EXPRENT_VAR) {
          VarVersionPair var = (((VarExprent) ((AssignmentExprent) exprent).getLeft())).getVarVersionPair();

          if (foundVar == null) {
            foundVar = var;
            found = (((VarExprent) ((AssignmentExprent) exprent).getLeft()));
          } else {
            if (!foundVar.equals(var)) {
              return false;
            }
          }
        } else if (exprent.type == Exprent.EXPRENT_EXIT && ((ExitExprent)exprent).getExitType() == ExitExprent.EXIT_RETURN) {
          // TODO: check for successors to dummy exit!
          return false; // Has a return, cannot be a switch statement
        }
      }
    }

    if (foundVar == null || found == null) {
      return false;
    }

    List<StatEdge> sucs = stat.getSuccessorEdges(StatEdge.TYPE_REGULAR);

    // TODO: should we be using getbasichead?
    if (!sucs.isEmpty()) {

      Statement suc = sucs.get(0).getDestination();
      if (suc.type == Statement.TYPE_BASICBLOCK) { // TODO: make basic block if it isn't found
        stat.setPhantom(true);

        for (Statement st : stat.getCaseStatements()) {
          Map<Exprent, YieldExprent> replacements = new HashMap<>();

          // No exprents, must not be a basicblock
          if (st.getExprents() == null) {
            continue;
          }

          for (Exprent e : st.getExprents()) {
            // Check for "var10000 = <value>" within the exprents
            if (e.type == Exprent.EXPRENT_ASSIGNMENT) {
              AssignmentExprent assign = ((AssignmentExprent) e);

              if (assign.getLeft().type == Exprent.EXPRENT_VAR) {
                if (((VarExprent)assign.getLeft()).getIndex() == foundVar.var) {
                  // Make yield with the right side of the assignment
                  replacements.put(assign, new YieldExprent(assign.getRight(), assign.getExprType()));
                }
              }
            }
          }

          // Replace exprents that we found
          if (!replacements.isEmpty()) {
            // Replace the assignments with yields, this allows 2 things:
            // 1)
            for (Map.Entry<Exprent, YieldExprent> entry : replacements.entrySet()) {
              st.replaceExprent(entry.getKey(), entry.getValue());
            }
          }
        }

        // TODO: move exprents from switch head to successor

        List<Exprent> exprents = suc.getExprents();

        VarExprent vExpr = new VarExprent(found.getIndex(), found.getVarType(), found.getProcessor());
        vExpr.setStack(true); // We want to inline
        AssignmentExprent toAdd = new AssignmentExprent(vExpr, new SwitchExprent(stat, found.getExprType()), null);

        exprents.add(0, toAdd);

        return true;
      }
    }

    return false;
  }

  public static boolean hasSwitchExpressions(RootStatement statement) {
    return statement.mt.getBytecodeVersion().hasSwitchExpressions() && DecompilerContext.getOption(IFernflowerPreferences.SWITCH_EXPRESSIONS);
  }
}
