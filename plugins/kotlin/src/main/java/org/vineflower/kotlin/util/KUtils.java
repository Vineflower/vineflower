package org.vineflower.kotlin.util;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinOptions;
import org.vineflower.kotlin.expr.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

  public static void appendVisibility(TextBuffer buf, ProtoBuf.Visibility visibility) {
    switch (visibility) {
      case LOCAL:
        buf.append("// QF: local property")
          .appendLineSeparator()
          .append("internal ");
        break;
      case PRIVATE_TO_THIS:
        buf.append("private ");
        break;
      case PUBLIC:
        if (DecompilerContext.getOption(KotlinOptions.SHOW_PUBLIC_VISIBILITY)) {
          buf.append("public ");
        }
        break;
      default:
        buf.append(visibility.name().toLowerCase())
          .append(' ');
    }
  }

  public static void removeArguments(InvocationExprent expr, VarType toRemove) {
    removeArguments(expr, toRemove, null);
  }

  public static void removeArguments(InvocationExprent expr, VarType toRemove, VarType replaceReturnType) {
    if (expr.getLstParameters().isEmpty()) {
      return;
    }

    if (expr.getLstParameters().size() == 1) {
      VarType argType = expr.getDescriptor().params[0];
      if (argType.equals(toRemove)) {
        expr.getLstParameters().clear();
      }
      return;
    }

    VarType[] params = expr.getDescriptor().params;
    List<Exprent> lst = expr.getLstParameters();
    for (int i = 0; i < params.length; i++) {
      VarType argType = params[i];
      if (argType.equals(toRemove)) {
        lst.set(i, null);
      }
    }
    lst.removeIf(Objects::isNull);

    String newDesc = expr.getStringDescriptor()
      .replace(toRemove.toString(), "");

      if (newDesc.endsWith(")")) {
        if (replaceReturnType == null) {
          throw new IllegalStateException("Invalid descriptor: " + newDesc);
        }

        newDesc += replaceReturnType.toString();
      }

    expr.setStringDescriptor(newDesc);
  }
}
