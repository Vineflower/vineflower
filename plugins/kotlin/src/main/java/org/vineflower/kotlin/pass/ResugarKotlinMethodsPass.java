package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper;
import org.jetbrains.java.decompiler.struct.match.MatchEngine;
import org.vineflower.kotlin.expr.KFunctionExprent;

import java.util.List;

public class ResugarKotlinMethodsPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    boolean res = false;

    DirectGraph digraph = FlattenStatementsHelper.build(ctx.getRoot());

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

  // ($x$ != null) ? $x$ : $y$
  private static final MatchEngine TERNARY_NULL_CHECK = new MatchEngine(
    "exprent type:function functype:ternary",
    " exprent position:1 ret:$x1$",
    " exprent position:2 ret:$y$",
    " exprent position:0 type:function functype:neq",
    "  exprent ret:$x$",
    "  exprent type:constant consttype:null"
  );
  
  // Reflection.getOrCreateKotlinClass($class$)
  private static final MatchEngine GET_KCLASS = new MatchEngine(
    "exprent type:invocation invclass:kotlin/jvm/internal/Reflection name:getOrCreateKotlinClass parameter:0:$class$"
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

    if (TERNARY_NULL_CHECK.match(ex)) {
      Exprent innerVal = (Exprent)TERNARY_NULL_CHECK.getVariableValue("$x1$");
      if (innerVal instanceof InvocationExprent && ((InvocationExprent)innerVal).isUnboxingCall()) {
        innerVal = ((InvocationExprent)innerVal).getInstance();
      }

      if (innerVal instanceof VarExprent && TERNARY_NULL_CHECK.getVariableValue("$x$").equals(innerVal)) {
        return new ResugarRes(new KFunctionExprent(KFunctionExprent.KFunctionType.IF_NULL, List.of(
          (Exprent) TERNARY_NULL_CHECK.getVariableValue("$x$"), (Exprent) TERNARY_NULL_CHECK.getVariableValue("$y$")
        ), null), false);
      }
    }

    if (GET_KCLASS.match(ex)) {
      return new ResugarRes(new KFunctionExprent(KFunctionExprent.KFunctionType.GET_KCLASS, List.of(
        (Exprent) GET_KCLASS.getVariableValue("$class$")
      ), null), false);
    }

    return new ResugarRes(null);
  }
}
