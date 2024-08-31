package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.vineflower.kotlin.expr.KVarExprent;
import org.vineflower.kotlin.util.KUtils;

import java.util.ArrayList;
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

    List<List<Exprent>> exprLists = new ArrayList<>();
    exprLists.add(stat.getExprents());
    exprLists.add(stat.getVarDefinitions());

    if (stat instanceof CatchAllStatement || stat instanceof CatchStatement) {
      // Special handling for catch declarations
      List<VarExprent> vars = stat instanceof CatchAllStatement ? ((CatchAllStatement)stat).getVars() : ((CatchStatement)stat).getVars();
      for (int i = 0; i < vars.size(); i++) {
        VarExprent expr = vars.get(i);
        KVarExprent map = new KVarExprent(expr);
        map.setExceptionType(true);
        vars.set(i, map);
      }

      if (stat instanceof CatchAllStatement catchAll && catchAll.getMonitor() != null) {
        catchAll.setMonitor(new KVarExprent(catchAll.getMonitor()));
      } else if (stat instanceof CatchStatement catchStat) {
        exprLists.add(catchStat.getResources());
      }
    } else if (stat instanceof DoStatement doStat) {
      exprLists.add(doStat.getInitExprentList());
      exprLists.add(doStat.getConditionExprentList());
      exprLists.add(doStat.getIncExprentList());
    } else if (stat instanceof IfStatement ifStat) {
      exprLists.add(ifStat.getHeadexprentList());
    } else if (stat instanceof SwitchStatement switchStat) {
      exprLists.addAll(switchStat.getCaseValues());
      exprLists.add(switchStat.getCaseGuards());
      exprLists.add(switchStat.getHeadexprentList());
    } else if (stat instanceof SynchronizedStatement syncStat) {
      exprLists.add(syncStat.getHeadexprentList());
    }

    for (List<Exprent> exprs : exprLists) {
      if (exprs == null) {
        continue;
      }

      for(int i = 0; i < exprs.size(); i++){
        Exprent expr = exprs.get(i);
        Exprent map = KUtils.replaceExprent(expr);

        if (map != null) {
          exprs.set(i, map);
          res = true;
        }
      }

      for (Exprent ex : exprs) {
        if (ex == null) {
          continue;
        }

        res |= replace(ex);
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
