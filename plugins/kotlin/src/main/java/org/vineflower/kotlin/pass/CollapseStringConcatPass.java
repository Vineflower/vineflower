package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.vineflower.kotlin.expr.KFunctionExprent;

import java.util.ArrayList;
import java.util.List;

public class CollapseStringConcatPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    return run(ctx.getRoot());
  }

  private static boolean run(Statement stat) {
    boolean res = false;

    for (Statement st : stat.getStats()) {
      res |= run(st);
    }

    List<Exprent> exprs = List.of();
    if (stat instanceof BasicBlockStatement) {
      exprs = stat.getExprents();
    } else if (stat instanceof IfStatement) {
      exprs = ((IfStatement)stat).getHeadexprentList();
    }

    for (Exprent ex : exprs) {
      res |= run(ex);
    }

    return res;
  }

  private static boolean run(Exprent ex) {
    boolean res = false;

    for (Exprent e : ex.getAllExprents()) {
      res |= run(e);
    }

    if (ex instanceof KFunctionExprent kex && kex.getFuncType() == FunctionExprent.FunctionType.STR_CONCAT) {
      List<Exprent> operands = new ArrayList<>(kex.getLstOperands());
      List<Exprent> lstOperands = kex.getLstOperands();
      for (Exprent child : operands) {
        if (child instanceof KFunctionExprent childKex && childKex.getFuncType() == FunctionExprent.FunctionType.STR_CONCAT) {
          lstOperands.addAll(lstOperands.indexOf(child), childKex.getLstOperands());
          lstOperands.remove(child);
          res = true;
        }
      }
    }

    return res;
  }
}
