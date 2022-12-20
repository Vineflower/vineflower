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
        Exprent condition = lstOperands.get(0);
        Exprent ifTrue = lstOperands.get(1);
        Exprent ifFalse = lstOperands.get(2);

        if (
          condition instanceof KFunctionExprent && ((KFunctionExprent) condition).getFuncType() == FunctionType.INSTANCEOF
          && ifTrue instanceof KFunctionExprent && ((KFunctionExprent) ifTrue).getFuncType() == FunctionType.CAST
          && ifFalse.getExprType() == VarType.VARTYPE_NULL
        ) {
          // Safe cast
          KFunctionExprent cast = (KFunctionExprent) ifTrue;
          buf.append(cast.getLstOperands().get(0).toJava(indent));
          buf.append(" as? ");
          buf.append(cast.getLstOperands().get(1).toJava(indent));
          return buf;
        }
      
        buf.pushNewlineGroup(indent, 1);
        buf.append("if (");
        buf.append(wrapOperandString(condition, true, indent))
          .append(")")
          .appendPossibleNewline(" ")
          .append(wrapOperandString(ifTrue, true, indent))
          .appendPossibleNewline(" ")
          .append("else")
          .appendPossibleNewline(" ")
          .append(wrapOperandString(ifFalse, true, indent));
        buf.popNewlineGroup();
      
        return buf;
      case INSTANCEOF:
        buf.append(wrapOperandString(lstOperands.get(0), true, indent))
          .append(" is ")
          .append(wrapOperandString(lstOperands.get(1), true, indent));
        
        return buf;
      case BOOL_NOT:
        // Special cases for `is` and `!is`
        // TODO: do the same for `in` and `!in`
        if (lstOperands.get(0) instanceof KFunctionExprent) {
          KFunctionExprent func = (KFunctionExprent) lstOperands.get(0);
          if (func.getFuncType() == FunctionExprent.FunctionType.INSTANCEOF) {
            buf.append(wrapOperandString(func.getLstOperands().get(0), true, indent))
              .append(" !is ")
              .append(wrapOperandString(func.getLstOperands().get(1), true, indent));
            return buf;
          }
        }
        break;
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
    }

    return buf.append(super.toJava(indent));
  }

  @Override
  public Exprent copy() {
    return new KFunctionExprent((FunctionExprent) super.copy());
  }
}
