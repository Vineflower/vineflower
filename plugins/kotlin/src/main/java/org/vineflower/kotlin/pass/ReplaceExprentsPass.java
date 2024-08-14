package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.vineflower.kotlin.expr.KVarExprent;
import org.vineflower.kotlin.util.KUtils;

import java.util.List;

public class ReplaceExprentsPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    return replace(ctx.getRoot());
  }

  private static boolean replace(Statement stat) {
    boolean res = false;

    for (Statement st : stat.getStats()) {
      res |= replace(st);
    }

    List<Exprent> exprs = List.of();
    if (stat instanceof BasicBlockStatement) {
      exprs = stat.getExprents();
    } else if (stat instanceof IfStatement) {
      exprs = ((IfStatement)stat).getHeadexprentList();
    } else if (stat instanceof CatchAllStatement || stat instanceof CatchStatement) {
      List<VarExprent> vars = stat instanceof CatchAllStatement ? ((CatchAllStatement)stat).getVars() : ((CatchStatement)stat).getVars();
      for (int i = 0; i < vars.size(); i++) {
        VarExprent expr = vars.get(i);
        KVarExprent map = new KVarExprent(expr);
        map.setExceptionType(true);
        vars.set(i, map);
      }
    }

    if (!exprs.isEmpty()) {
      for(int i = 0; i < exprs.size(); i++){
        Exprent expr = exprs.get(i);
        Exprent map = KUtils.replaceExprent(expr);

        if (map != null) {
          exprs.set(i, map);
          res = true;
        }
      }

      for (Exprent ex : exprs) {
        res |= replace(ex);
      }
    }

    for (int i = 0; i < stat.getVarDefinitions().size(); i++) {
      Exprent expr = stat.getVarDefinitions().get(i);

      Exprent map = KUtils.replaceExprent(expr);
      if (map != null) {
        stat.getVarDefinitions().set(i, map);

        res = true;
      }
    }

    return res;
  }

  private static boolean replace(Exprent expr) {
    boolean res = false;

    for (Exprent ex : expr.getAllExprents()) {
      res |= replace(ex);
      Exprent map = KUtils.replaceExprent(ex);

      if (map != null) {
        expr.replaceExprent(ex, map);
        res = true;
      }
    }

    return res;
  }
}
