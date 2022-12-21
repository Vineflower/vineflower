package org.quiltmc.quiltflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.passes.Pass;
import org.jetbrains.java.decompiler.api.passes.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.match.MatchEngine;
import org.quiltmc.quiltflower.kotlin.expr.KFunctionExprent;

import java.util.List;

public class ResugarKotlinMethodsPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    boolean res = false;

    DirectGraph digraph = new FlattenStatementsHelper().buildDirectGraph(ctx.getRoot());

    for (DirectNode nd : digraph.nodes) {
      List<Exprent> exprs = nd.exprents;
      for (Exprent ex : exprs) {
        res |= resugarExprs(ex);
      }

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
    }

    return res;
  }

  private static boolean resugarExprs(Exprent expr) {
    boolean res = false;

    for (Exprent ex : expr.getAllExprents()) {
      res |= resugarExprs(ex);

      Exprent map = resugarExpr(ex).expr;

      if (map != null) {
        expr.replaceExprent(ex, map);
        res = true;
      }
    }

    return res;
  }


  private static final MatchEngine[] NONNULL_INTRINSICS = {
    new MatchEngine(
      "exprent type:invocation invclass:kotlin/jvm/internal/Intrinsics signature:checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V"
    ),
    new MatchEngine(
      "exprent type:invocation invclass:kotlin/jvm/internal/Intrinsics signature:checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V"
    ),
    new MatchEngine(
      "exprent type:invocation invclass:kotlin/jvm/internal/Intrinsics signature:checkNotNull(Ljava/lang/Object;Ljava/lang/String;)V"
    ),
    new MatchEngine(
      "exprent type:invocation invclass:kotlin/jvm/internal/Intrinsics signature:checkNotNull(Ljava/lang/Object;)V"
    )
  };

  // Intrinsics.areEqual($lhs$, $rhs$)
  private static final MatchEngine EQUAL_INTRINSIC = new MatchEngine(
    "exprent type:invocation invclass:kotlin/jvm/internal/Intrinsics name:areEqual parameter:0:$lhs$ parameter:1:$rhs$"
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
    for (MatchEngine engine : NONNULL_INTRINSICS) {
      if (engine.match(ex)) {
        return new ResugarRes(null, true);
      }
    }

    if (EQUAL_INTRINSIC.match(ex)) {
      return new ResugarRes(new KFunctionExprent(FunctionExprent.FunctionType.EQ, List.of(
        (Exprent) EQUAL_INTRINSIC.getVariableValue("$lhs$"), (Exprent) EQUAL_INTRINSIC.getVariableValue("$rhs$")
      ), null), false);
    }

    return new ResugarRes(null);
  }
}
