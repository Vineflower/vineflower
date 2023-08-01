package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.util.KTypes;

public class KFieldExprent extends FieldExprent implements KExprent {
  public KFieldExprent(FieldExprent field) {
    super(field.getName(), field.getClassname(), field.isStatic(), field.getInstance(), field.getDescriptor(), field.bytecode);
  }

  @Override
  public TextBuffer toJava(int indent) {
    if (getName().equals("TYPE") && ExprUtil.PRIMITIVE_TYPES.containsKey(getClassname())) {
      TextBuffer buf = new TextBuffer();
      VarType type = new VarType(getClassname(), true);
      buf.append(KTypes.getKotlinType(type));
      buf.append("::class.javaPrimitiveType");
      return buf;
    }
    return super.toJava(indent);
  }
}
