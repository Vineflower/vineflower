package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.util.KTypes;

public class KInvocationExprent extends InvocationExprent implements KExprent {
  public KInvocationExprent(InvocationExprent expr) {
    super(expr);
  }

  @Override
  public TextBuffer toJava(int indent) {
    if (KTypes.isFunctionType(new VarType(getClassname(), true))) {
      TextBuffer buf = new TextBuffer();
      TextBuffer instanceBuf = getInstance().toJava(indent);
      if (getInstance().getPrecedence() > getPrecedence()) {
        instanceBuf.enclose("(", ")");
      }
      buf.append(instanceBuf);
      if (getLstParameters().isEmpty()) {
        buf.appendMethod("()", false, getClassname(), getName(), getDescriptor());
        buf.addBytecodeMapping(bytecode);
        return buf;
      }

      buf.appendMethod("(", false, getClassname(), getName(), getDescriptor());
      buf.append(appendParamList(indent));
      buf.appendMethod(")", false, getClassname(), getName(), getDescriptor());
      buf.addBytecodeMapping(bytecode);
      return buf;
    }

    return super.toJava(indent);
  }

  @Override
  public Exprent copy() {
    return new KInvocationExprent(this);
  }
}
