package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.util.KTypes;
import org.vineflower.kotlin.util.KUtils;

public class KAnnotationExprent extends AnnotationExprent implements KExprent {
  public enum UseSiteTarget {
    FILE,
    PROPERTY,
    FIELD,
    GET,
    SET,
    RECEIVER,
    PARAM,
    SETPARAM,
    DELEGATE,
  }

  private final UseSiteTarget useSiteTarget;

  public KAnnotationExprent(AnnotationExprent expr) {
    this(expr, null);
  }

  public KAnnotationExprent(AnnotationExprent expr, UseSiteTarget useSiteTarget) {
    super(expr.getClassName(), expr.getParNames(), KUtils.replaceExprents(expr.getParValues()));
    bytecode = expr.bytecode;
    this.useSiteTarget = useSiteTarget;
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buffer = new TextBuffer();
    buffer.addBytecodeMapping(bytecode);

    buffer.appendIndent(indent).append('@');

    if (useSiteTarget != null) {
      buffer.append(useSiteTarget.name().toLowerCase()).append(':');
    }

    VarType type = new VarType(getClassName(), true);
    buffer.append(KTypes.getKotlinType(type));

    switch (getAnnotationType()) {
      case SINGLE_ELEMENT:
        buffer.append('(').appendPossibleNewline();
        writeAnnotationValue(getParValues().get(0), buffer);
        buffer.append(')');
        break;
      case NORMAL:
        buffer.append('(').appendPossibleNewline();
        boolean first = true;
        for (int i = 0; i < getParValues().size(); i++) {
          if (first) {
            first = false;
          } else {
            buffer.append(",").appendPossibleNewline(" ");
          }
          buffer
            .append(getParNames().get(i))
            .append(" = ");
          writeAnnotationValue(getParValues().get(i), buffer);
        }
        buffer.append(')');
        break;
    }

    return buffer;
  }

  public static void writeAnnotationValue(Exprent expr, TextBuffer buffer) {
    if (expr instanceof FieldExprent) {
      // Enum value
      FieldExprent fieldExprent = (FieldExprent) expr;
      VarType type = new VarType(fieldExprent.getClassname(), true);
      DecompilerContext.getImportCollector().getShortName(fieldExprent.getClassname(), true);

      buffer
        .append(KTypes.getKotlinType(type))
        .append(".")
        .append(fieldExprent.getName());
    } else if (expr instanceof NewExprent) {
      // Array value
      NewExprent newExprent = (NewExprent) expr;
      buffer.append('[');
      boolean first = true;
      for (Exprent exprent : newExprent.getLstArrayElements()) {
        if (first) {
          first = false;
        } else {
          buffer.append(", ");
        }
        writeAnnotationValue(exprent, buffer);
      }
      buffer.append(']');
    } else if (expr instanceof AnnotationExprent) {
      AnnotationExprent annotationExprent = (AnnotationExprent) expr;
      buffer.append('@');
      VarType type = new VarType(annotationExprent.getClassName(), true);
      buffer.append(KTypes.getKotlinType(type));

      switch (annotationExprent.getAnnotationType()) {
        case SINGLE_ELEMENT:
          buffer.append('(');
          writeAnnotationValue(annotationExprent.getParValues().get(0), buffer);
          buffer.append(')');
          break;
        case NORMAL:
          buffer.append('(');
          boolean first = true;
          for (int i = 0; i < annotationExprent.getParValues().size(); i++) {
            if (first) {
              first = false;
            } else {
              buffer.append(", ");
            }
            buffer
              .append(annotationExprent.getParNames().get(i))
              .append(" = ");
            writeAnnotationValue(annotationExprent.getParValues().get(i), buffer);
          }
          buffer.append(')');
          break;
      }
    } else if (expr instanceof ConstExprent) {
      ConstExprent constExprent = (ConstExprent) expr;
      if (constExprent.getConstType().equals(VarType.VARTYPE_CLASS)) {
        VarType type = new VarType((String) constExprent.getValue(), true);
        DecompilerContext.getImportCollector().getShortName((String) constExprent.getValue(), true);
        buffer.append(KTypes.getKotlinType(type))
          .append("::class");
      } else {
        buffer.append(constExprent.toJava(0));
      }
    }
  }
}
