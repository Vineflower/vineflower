package org.quiltmc.quiltflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.passes.Pass;
import org.jetbrains.java.decompiler.api.passes.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.match.MatchEngine;

import java.util.List;

public class ResugarKotlinMethodsPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    return resugarStats(ctx.getRoot());
  }

  private static boolean resugarStats(Statement stat) {
    boolean res = false;

    for (Statement st : stat.getStats()) {
      res |= resugarStats(st);
    }

    if (stat instanceof BasicBlockStatement) {
      List<Exprent> exprs = stat.getExprents();

      for (int i = 0; i < exprs.size(); i++) {
        Exprent expr = exprs.get(i);

        ResugarRes exprRes = resugarExpr(expr);
        if (exprRes.remove) {
          exprs.remove(i);
          i--;
          res = true;
        } else if (exprRes.expr != null) {
          exprs.set(i, exprRes.expr);
          res = true;
        }

      }

      for (Exprent ex : exprs) {
        res |= resugarExprs(ex);
      }
    }

    return res;
  }

  private static boolean resugarExprs(Exprent expr) {
    boolean res = false;

    for (Exprent ex : expr.getAllExprents()) {
      Exprent map = resugarExpr(ex).expr;

      if (map != null) {
        expr.replaceExprent(ex, map);
      } else {
        res |= resugarExprs(ex);
      }
    }

    return res;
  }

  private static final MatchEngine INTRINSICS_CHECKNONNULL = new MatchEngine(
    "exprent type:invocation invclass:kotlin/jvm/internal/Intrinsics signature:checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V"
  );

  private static class ResugarRes {
    public final Exprent expr;
    public final boolean remove;

    public ResugarRes(Exprent expr) {
      this(expr, false);
    }

    public ResugarRes(Exprent expr, boolean remove) {
      this.expr = expr;
      this.remove = remove;
    }
  }

  private static ResugarRes resugarExpr(Exprent ex) {
    if (INTRINSICS_CHECKNONNULL.match(ex)) {
      return new ResugarRes(null, true);
    }

    return new ResugarRes(null);
  }
}
