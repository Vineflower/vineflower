package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.token.TokenType;
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

    TextBuffer inner = new TextBuffer();
    inner.appendPunctuation("@");

    if (useSiteTarget != null) {
      inner.appendKeyword(useSiteTarget.name().toLowerCase()).appendPunctuation(':');
    }

    VarType type = new VarType(getClassName(), true);
    inner.appendClass(KTypes.getKotlinType(type), false, type.value);

    switch (getAnnotationType()) {
      case SINGLE_ELEMENT -> {
        inner.appendPunctuation('(');
        inner.pushNewlineGroup(indent, 1).appendPossibleNewline();
        writeAnnotationValue(getParValues().get(0), inner);
        inner.appendPossibleNewline("", true).popNewlineGroup();
        inner.appendPunctuation(')');
      }
      case NORMAL -> {
        inner.appendPunctuation('(');
        inner.pushNewlineGroup(indent, 1).appendPossibleNewline();
        boolean first = true;
        for (int i = 0; i < getParValues().size(); i++) {
          if (first) {
            first = false;
          } else {
            inner.appendPunctuation(",").appendPossibleNewline(" ");
          }
          inner
            .append(getParNames().get(i), TokenType.PARAMETER)
            .appendWhitespace(" ").appendPunctuation('=').appendWhitespace(" ");
          writeAnnotationValue(getParValues().get(i), inner);
        }
        inner.appendPossibleNewline("", true).popNewlineGroup();
        inner.appendPunctuation(')');
      }
    }

    return buffer.appendIndent(indent).appendAnnotation(inner);
  }

  public static void writeAnnotationValue(Exprent expr, TextBuffer buffer) {
    if (expr instanceof FieldExprent fieldExprent) {
      // Enum value
      VarType type = new VarType(fieldExprent.getClassname(), true);
      DecompilerContext.getImportCollector().getShortName(fieldExprent.getClassname(), true);

      buffer
        .appendClass(KTypes.getKotlinType(type), false, type.value)
        .appendPunctuation(".")
        .appendField(fieldExprent.getName(), false, fieldExprent.getClassname(), fieldExprent.getName(), fieldExprent.getDescriptor());
    } else if (expr instanceof NewExprent newExprent) {
      // Array value
      buffer.appendPunctuation('[');
      boolean first = true;
      for (Exprent exprent : newExprent.getLstArrayElements()) {
        if (first) {
          first = false;
        } else {
          buffer.appendPunctuation(",").appendWhitespace(" ");
        }
        writeAnnotationValue(exprent, buffer);
      }
      buffer.appendPunctuation(']');
    } else if (expr instanceof AnnotationExprent annotationExprent) {
      TextBuffer inner = new TextBuffer();
      inner.appendPunctuation('@');
      VarType type = new VarType(annotationExprent.getClassName(), true);
      inner.appendClass(KTypes.getKotlinType(type), false, type.value);

      switch (annotationExprent.getAnnotationType()) {
        case SINGLE_ELEMENT -> {
          inner.appendPunctuation('(');
          writeAnnotationValue(annotationExprent.getParValues().get(0), inner);
          inner.appendPunctuation(')');
        }
        case NORMAL -> {
          inner.appendPunctuation('(');
          boolean first = true;
          for (int i = 0; i < annotationExprent.getParValues().size(); i++) {
            if (first) {
              first = false;
            } else {
              inner.appendPunctuation(",").appendWhitespace(" ");
            }
            inner
              .append(annotationExprent.getParNames().get(i), TokenType.PARAMETER)
              .appendWhitespace(" ").appendPunctuation('=').appendWhitespace(" ");
            writeAnnotationValue(annotationExprent.getParValues().get(i), inner);
          }
          inner.appendPunctuation(')');
        }
      }
      buffer.appendAnnotation(inner);
    } else if (expr instanceof ConstExprent constExprent) {
      if (constExprent.getConstType().equals(VarType.VARTYPE_CLASS)) {
        VarType type = new VarType((String) constExprent.getValue(), true);
        DecompilerContext.getImportCollector().getShortName((String) constExprent.getValue(), true);
        buffer.appendClass(KTypes.getKotlinType(type), false, type.value)
          .appendPunctuation("::")
          .appendKeyword("class");
      } else {
        buffer.append(constExprent.toJava(0));
      }
    }
  }
}
