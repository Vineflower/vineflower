package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.vineflower.kotlin.KotlinOptions;
import org.vineflower.kotlin.expr.KFunctionExprent;

import java.util.ArrayList;
import java.util.List;

public class CollapseStringConcatPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    return run(ctx.getRoot());
  }

  private static boolean run(Statement stat) {
    if (!DecompilerContext.getOption(KotlinOptions.COLLAPSE_STRING_CONCATENATION)) {
      return false;
    }

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
  
  private static boolean isStringConcat(KFunctionExprent kex) {
    return kex.getAnyFunctionType() == FunctionExprent.FunctionType.STR_CONCAT ||
            kex.getAnyFunctionType() == KFunctionExprent.KFunctionType.STR_TEMPLATE;
  }

  private static boolean run(Exprent ex) {
    boolean res = false;

    for (Exprent e : ex.getAllExprents()) {
      res |= run(e);
    }

    if (ex instanceof KFunctionExprent kex && isStringConcat(kex)) {
      List<Exprent> operands = new ArrayList<>(kex.getLstOperands());
      List<Exprent> lstOperands = kex.getLstOperands();
      for (Exprent child : operands) {
        if (child instanceof KFunctionExprent childKex && isStringConcat(childKex)) {
          lstOperands.addAll(lstOperands.indexOf(child), childKex.getLstOperands());
          lstOperands.remove(child);
        }
      }
      kex.setFuncType(KFunctionExprent.KFunctionType.STR_TEMPLATE);
      res = true;
    }

    return res;
  }
}
