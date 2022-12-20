package org.quiltmc.quiltflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.List;

public class KFunctionExprent extends FunctionExprent {
  public KFunctionExprent(FunctionExprent func) {
    super(func.getFuncType(), func.getLstOperands(), func.bytecode);
    switch (getFuncType()) {
      case IMM:
      case MMI:
      case IPP:
      case PPI:
        setImplicitType(func.getExprType());
        break;
    }
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
      case BIT_NOT:
        buf.append(wrapOperandString(lstOperands.get(0), true, indent));
        return buf.append(".inv()");
      case AND: // Bitwise AND
        buf.append(wrapOperandString(lstOperands.get(0), true, indent)).append(" and ")
          .append(wrapOperandString(lstOperands.get(1), true, indent));
        return buf;
      case OR:
        buf.append(wrapOperandString(lstOperands.get(0), true, indent)).append(" or ")
          .append(wrapOperandString(lstOperands.get(1), true, indent));
        return buf;
      case XOR:
        buf.append(wrapOperandString(lstOperands.get(0), true, indent)).append(" xor ")
          .append(wrapOperandString(lstOperands.get(1), true, indent));
        return buf;
      case SHL:
        buf.append(wrapOperandString(lstOperands.get(0), true, indent)).append(" shl ")
          .append(wrapOperandString(lstOperands.get(1), true, indent));
        return buf;
      case SHR:
        buf.append(wrapOperandString(lstOperands.get(0), true, indent)).append(" shr ")
          .append(wrapOperandString(lstOperands.get(1), true, indent));
        return buf;
      case USHR:
        buf.append(wrapOperandString(lstOperands.get(0), true, indent)).append(" ushr ")
          .append(wrapOperandString(lstOperands.get(1), true, indent));
        return buf;
      case BOOL_NOT:
        // Special cases for `is` and `!is`
        // TODO: do the same for `in` and `!in`
        if (lstOperands.get(0) instanceof KFunctionExprent) {
          KFunctionExprent func = (KFunctionExprent) lstOperands.get(0);
          if (func.getFuncType() == FunctionExprent.FunctionType.INSTANCEOF) {
            TextBuffer buf2 = func.toJava(indent);
            buf.append(buf2.convertToStringAndAllowDataDiscard().replace(" is ", " !is "));
            return buf;
          }
        }
    }

    return buf.append(super.toJava(indent));
  }

  @Override
  public Exprent copy() {
    return new KFunctionExprent((FunctionExprent) super.copy());
  }
}
