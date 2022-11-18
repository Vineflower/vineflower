package org.quiltmc.quiltflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.passes.Pass;
import org.jetbrains.java.decompiler.api.passes.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.quiltmc.quiltflower.kotlin.expr.KVarExprent;

public class ReplaceVarExprentsPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    return replace(ctx.getRoot());
  }

  private static boolean replace(Statement stat) {
    boolean res = false;

    for (Statement st : stat.getStats()) {
      res |= replace(st);
    }

    if (stat instanceof BasicBlockStatement) {
      for (Exprent ex : stat.getExprents()) {
        res |= replace(ex);
      }
    }

    return res;
  }

  private static boolean replace(Exprent expr) {
    boolean res = false;

    for (Exprent ex : expr.getAllExprents()) {
      if (ex instanceof VarExprent) {
        expr.replaceExprent(ex, new KVarExprent((VarExprent) ex));
      } else {
        res |= replace(ex);
      }
    }

    return res;
  }
}
