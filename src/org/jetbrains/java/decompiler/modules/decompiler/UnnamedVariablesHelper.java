package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;

public class UnnamedVariablesHelper {

  public static boolean setUnnamedVariables(RootStatement root) {
    return setUnnamedVariables(root, root);
  }

  private static boolean setUnnamedVariables(Statement statement, RootStatement root) {
    boolean result = false;
    if (statement.getExprents() != null) {
      for (Exprent exp : statement.getExprents()) {
        result |= setUnnamedLocalVariables(exp, root);
      }
    } else {
      for (Statement stat : statement.getStats()) {
        result |= setUnnamedVariables(stat, root);
      }
    }

    if (statement instanceof DoStatement doStat && !doStat.getInitExprentList().isEmpty()) {
      if (doStat.getInitExprent() instanceof VarExprent def
          && def.getLVT() == null
          && !def.isVarReferenced(doStat)) {
        def.setUnnamedVar(true);
        result = true;
      }

      result |= setUnnamedLocalVariables(doStat.getInitExprent(), root);
    }

    if (statement instanceof CatchStatement catchStat) {
      for (Exprent resource : catchStat.getResources()) {
        result |= setUnnamedLocalVariables(resource, root);
      }

      for (VarExprent catchVar : catchStat.getVars()) {
        if (catchVar.getLVT() == null
            && !catchVar.isVarReferenced(catchStat)) {
          catchVar.setUnnamedVar(true);
          result = true;
        }
      }
    }
    return result;
  }

  public static boolean setUnnamedLocalVariables(Exprent exp, RootStatement root) {
    if (exp instanceof AssignmentExprent assign
        && assign.getLeft() instanceof VarExprent def
        && def.isDefinition()
        && def.getLVT() == null
        && !def.isVarReferenced(root)) {
      def.setUnnamedVar(true);
      return true;
    }
    return false;
  }
}
