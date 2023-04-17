package org.quiltmc.quiltflower.kotlin.util;

import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.quiltmc.quiltflower.kotlin.expr.*;

import java.util.ArrayList;
import java.util.List;

public class KUtils {
  public static List<? extends Exprent> replaceExprents(List<? extends Exprent> exprs) {
    List<Exprent> res = new ArrayList<>();

    for (Exprent expr : exprs) {
      Exprent map = replaceExprent(expr);
      res.add(map != null ? map : expr);
    }

    return res;
  }

  public static Exprent replaceExprent(Exprent ex) {
    if (ex instanceof KExprent) {
      return ex;
    }

    if (ex instanceof FunctionExprent) {
      return new KFunctionExprent((FunctionExprent) ex);
    } else if (ex instanceof VarExprent) {
      return new KVarExprent((VarExprent) ex);
    } else if (ex instanceof InvocationExprent) {
      return new KInvocationExprent((InvocationExprent) ex);
    } else if (ex instanceof ConstExprent) {
      return new KConstExprent((ConstExprent) ex);
    } else if (ex instanceof FieldExprent) {
      return new KFieldExprent((FieldExprent) ex);
    } else if (ex instanceof AnnotationExprent) {
      return new KAnnotationExprent((AnnotationExprent) ex);
    }

    return null;
  }
}
