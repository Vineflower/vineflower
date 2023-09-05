package org.vineflower.kotlin.util;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinPreferences;
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
        String showPublicVisibility = KotlinPreferences.getPreference(KotlinPreferences.SHOW_PUBLIC_VISIBILITY);
        if (Objects.equals(showPublicVisibility, "1")) {
          buf.append("public ");
        }
        break;
      default:
        buf.append(visibility.name().toLowerCase())
          .append(' ');
    }
  }
}
