package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.struct.KType;
import org.vineflower.kotlin.util.KTypes;

public class KConstExprent extends ConstExprent implements KExprent {
  public KConstExprent(ConstExprent exprent) {
    super(exprent.getConstType(), exprent.getValue(), exprent.isBoolPermitted(), exprent.bytecode);
  }

  @Override
  public TextBuffer toJava(int indent) {
    if (getValue() == null) {
      if (getConstType().equals(VarType.VARTYPE_NULL)) {
        return super.toJava(indent);
      }

      //TODO: what happened?
      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(bytecode);
      buf.appendTypeName(KTypes.getKotlinType(getConstType()), getConstType());
      return buf;
    }

    if (getConstType().equals(VarType.VARTYPE_CLASS)) {
      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(bytecode);

      String value = getValue().toString();
      VarType type = new VarType(value, !value.startsWith("["));
      buf.appendCastTypeName(type)
        .appendOperator("::")
        .appendKeyword("class")
        .appendPunctuation(".");
      if (ExprUtil.PRIMITIVE_TYPES.containsKey(value)) {
        buf.appendMethod("javaObjectType", false, "kotlin/reflect/KClass", "javaObjectType", "()Ljava/lang/Class;");
      } else {
        buf.appendMethod("java", false, "kotlin/reflect/KClass", "java", "()Ljava/lang/Class;");
      }
      return buf;
    } else if (getConstType() instanceof KType ktype && ktype.isUnsignedType) {
      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(bytecode);
      if (ktype.equals(KType.ULONG)) {
        buf.appendNumber(Long.toUnsignedString(((Long) getValue()))).appendNumber("uL");
      } else if (ktype.equals(KType.UBYTE)) {
        buf.appendNumber(getIntValue() & 0xff).appendNumber("u");
      } else if (ktype.equals(KType.USHORT)) {
        buf.appendNumber(getIntValue() & 0xffff).appendNumber("u");
      } else if (ktype.equals(KType.UINT)) {
        buf.appendNumber(Integer.toUnsignedString(getIntValue())).appendNumber("u");
      } else {
        throw new IllegalStateException("Unknown unsigned type: " + ktype);
      }
      return buf;
    }
    
    return super.toJava(indent);
  }
}
