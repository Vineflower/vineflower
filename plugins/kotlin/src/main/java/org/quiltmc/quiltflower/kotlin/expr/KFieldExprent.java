package org.quiltmc.quiltflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.util.TextBuffer;

public class KFieldExprent extends FieldExprent {
  public KFieldExprent(FieldExprent field) {
    super(field.getName(), field.getClassname(), field.isStatic(), field.getInstance(), field.getDescriptor(), field.bytecode);
  }

  @Override
  public TextBuffer toJava(int indent) {
    if (getName().equals("TYPE") && ExprUtil.PRIMITIVE_TYPES.containsKey(getClassname())) {
      TextBuffer buf = new TextBuffer();
      buf.append(getClassname().substring(10));
      buf.append("::class.javaPrimitiveType");
      return buf;
    }
    return super.toJava(indent);
  }
}
