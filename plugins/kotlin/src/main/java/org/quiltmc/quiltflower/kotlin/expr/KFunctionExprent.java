package org.quiltmc.quiltflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.List;

public class KFunctionExprent extends FunctionExprent {
  public KFunctionExprent(FunctionExprent func) {
    super(func.getFuncType(), func.getLstOperands(), func.bytecode);
  }

  @Override
  public TextBuffer toJava(int indent) {
  
    TextBuffer buf = new TextBuffer();
    buf.addBytecodeMapping(this.bytecode);
    List<Exprent> lstOperands = getLstOperands();
  
    switch(getFuncType()){
      case TERNARY:
      
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
      case INSTANCEOF:
        buf.append(wrapOperandString(lstOperands.get(0), true, indent))
          .append(" is ")
          .append(wrapOperandString(lstOperands.get(1), true, indent));
        
        return buf;
      case CAST:
        if (!doesCast()) {
          return buf.append(lstOperands.get(0).toJava(indent));
        }
        buf.append(wrapOperandString(lstOperands.get(0), true, indent)).append(" as ").append(lstOperands.get(1).toJava(indent));
        return buf;
    }

    return buf.append(super.toJava(indent));
  }
}
