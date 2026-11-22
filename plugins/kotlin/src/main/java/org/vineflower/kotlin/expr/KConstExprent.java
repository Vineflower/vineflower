package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.struct.KType;
import org.vineflower.kotlin.util.KTypes;
import org.vineflower.kotlin.util.KUtils;

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

      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(bytecode);
      buf.append(KTypes.getKotlinType(getConstType()));
      return buf;
    }

    if (getConstType().equals(VarType.VARTYPE_CLASS)) {
      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(bytecode);

      String value = getValue().toString();
      VarType type = new VarType(value, !value.startsWith("["));
      buf.appendCastTypeName(type).append("::class.java");
      if (ExprUtil.PRIMITIVE_TYPES.containsKey(value)) {
        buf.append("ObjectType"); // Primitive boxes require javaObjectType
      }
      return buf;
    } else if (getConstType() instanceof KType ktype && ktype.isUnsignedType) {
      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(bytecode);
      if (ktype.equals(KType.ULONG)) {
        buf.append(Long.toUnsignedString(((Long) getValue()))).append("uL");
      } else if (ktype.equals(KType.UBYTE)) {
        buf.append(getIntValue() & 0xff).append("u");
      } else if (ktype.equals(KType.USHORT)) {
        buf.append(getIntValue() & 0xffff).append("u");
      } else if (ktype.equals(KType.UINT)) {
        buf.append(Integer.toUnsignedString(getIntValue())).append("u");
      } else {
        throw new IllegalStateException("Unknown unsigned type: " + ktype);
      }
      return buf;
    }
    
    return super.toJava(indent);
  }
}
