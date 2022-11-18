package org.quiltmc.quiltflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.collections.ListStack;

import java.util.BitSet;
import java.util.List;

public class KFunctionExprent extends FunctionExprent {
  public KFunctionExprent(FunctionExprent func) {
    super(func.getFuncType(), func.getLstOperands(), func.bytecode);
  }

  @Override
  public TextBuffer toJava(int indent) {

    if (getFuncType() == FunctionType.TERNARY) {
      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(this.bytecode);

      List<Exprent> lstOperands = getLstOperands();

      buf.pushNewlineGroup(indent, 1);
      buf.append("if (");
      buf.append(wrapOperandString(lstOperands.get(0), true, indent))
        .append(")")
        .appendPossibleNewline(" ")
        .append(wrapOperandString(lstOperands.get(1), true, indent))
        .appendPossibleNewline(" ")
        .append("else")
        .appendPossibleNewline(" ")
        .append(wrapOperandString(lstOperands.get(2), true, indent));
      buf.popNewlineGroup();
      
      return buf;
    }

    return super.toJava(indent);
  }
}
