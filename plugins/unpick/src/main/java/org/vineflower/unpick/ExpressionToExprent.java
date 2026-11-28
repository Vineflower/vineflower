package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.Literal;
import org.vineflower.unpick.parser.tree.expr.BinaryExpression;
import org.vineflower.unpick.parser.tree.expr.CastExpression;
import org.vineflower.unpick.parser.tree.expr.ExpressionVisitor;
import org.vineflower.unpick.parser.tree.expr.FieldExpression;
import org.vineflower.unpick.parser.tree.expr.LiteralExpression;
import org.vineflower.unpick.parser.tree.expr.UnaryExpression;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ExpressionToExprent extends ExpressionVisitor {
  @Nullable
  private final MethodWrapper nonStaticMethodWrapper;
  @Nullable
  private final BitSet bytecodeOffsets;
  public Exprent result;

  public ExpressionToExprent(@Nullable MethodWrapper nonStaticMethodWrapper, @Nullable BitSet bytecodeOffsets) {
    this.nonStaticMethodWrapper = nonStaticMethodWrapper;
    this.bytecodeOffsets = bytecodeOffsets;
  }

  @Override
  public void visitBinaryExpression(BinaryExpression binaryExpression) {
    List<Exprent> operands = new ArrayList<>(2);
    binaryExpression.lhs.accept(this);
    operands.add(result);
    binaryExpression.rhs.accept(this);
    operands.add(result);

    FunctionExprent.FunctionType funcType = switch (binaryExpression.operator) {
      case BIT_OR -> FunctionExprent.FunctionType.OR;
      case BIT_XOR -> FunctionExprent.FunctionType.XOR;
      case BIT_AND -> FunctionExprent.FunctionType.AND;
      case BIT_SHIFT_LEFT -> FunctionExprent.FunctionType.SHL;
      case BIT_SHIFT_RIGHT -> FunctionExprent.FunctionType.SHR;
      case BIT_SHIFT_RIGHT_UNSIGNED -> FunctionExprent.FunctionType.USHR;
      case ADD -> FunctionExprent.FunctionType.ADD;
      case SUBTRACT -> FunctionExprent.FunctionType.SUB;
      case MULTIPLY -> FunctionExprent.FunctionType.MUL;
      case DIVIDE -> FunctionExprent.FunctionType.DIV;
      case MODULO -> FunctionExprent.FunctionType.REM;
    };
    result = new FunctionExprent(funcType, operands, bytecodeOffsets);
  }

  @Override
  public void visitCastExpression(CastExpression castExpression) {
    ExpressionEvaluator evaluator = new ExpressionEvaluator();
    castExpression.operand.accept(evaluator); // cannot throw, the entire expression was already evaluated before we reached this point
    DataType castFromType = DataTypeUtils.getDataType(evaluator.result);
    boolean primitiveCast = DataTypeUtils.isPrimitive(castExpression.castType) && DataTypeUtils.isPrimitive(castFromType);

    castExpression.operand.accept(this);
    List<Exprent> operands = new ArrayList<>(primitiveCast ? 1 : 2);
    operands.add(result);

    if (primitiveCast) {
      List<FunctionExprent.FunctionType> funcTypes = switch (castFromType) {
        case BYTE, SHORT, INT, CHAR -> switch (castExpression.castType) {
          case BYTE -> List.of(FunctionExprent.FunctionType.I2B);
          case SHORT -> List.of(FunctionExprent.FunctionType.I2S);
          case INT -> List.of();
          case LONG -> List.of(FunctionExprent.FunctionType.I2L);
          case FLOAT -> List.of(FunctionExprent.FunctionType.I2F);
          case DOUBLE -> List.of(FunctionExprent.FunctionType.I2D);
          case CHAR -> List.of(FunctionExprent.FunctionType.I2C);
          default -> throw new UnsupportedOperationException("Unknown primitive type");
        };
        case LONG -> switch (castExpression.castType) {
          case BYTE -> List.of(FunctionExprent.FunctionType.L2I, FunctionExprent.FunctionType.I2B);
          case SHORT -> List.of(FunctionExprent.FunctionType.L2I, FunctionExprent.FunctionType.I2S);
          case INT -> List.of(FunctionExprent.FunctionType.L2I);
          case LONG -> List.of();
          case FLOAT -> List.of(FunctionExprent.FunctionType.L2F);
          case DOUBLE -> List.of(FunctionExprent.FunctionType.L2D);
          case CHAR -> List.of(FunctionExprent.FunctionType.L2I, FunctionExprent.FunctionType.I2C);
          default -> throw new UnsupportedOperationException("Unknown primitive type");
        };
        case FLOAT -> switch (castExpression.castType) {
          case BYTE -> List.of(FunctionExprent.FunctionType.F2I, FunctionExprent.FunctionType.I2B);
          case SHORT -> List.of(FunctionExprent.FunctionType.F2I, FunctionExprent.FunctionType.I2S);
          case INT -> List.of(FunctionExprent.FunctionType.F2I);
          case LONG -> List.of(FunctionExprent.FunctionType.F2L);
          case FLOAT -> List.of();
          case DOUBLE -> List.of(FunctionExprent.FunctionType.F2D);
          case CHAR -> List.of(FunctionExprent.FunctionType.F2I, FunctionExprent.FunctionType.I2C);
          default -> throw new UnsupportedOperationException("Unknown primitive type");
        };
        case DOUBLE -> switch (castExpression.castType) {
          case BYTE -> List.of(FunctionExprent.FunctionType.D2I, FunctionExprent.FunctionType.I2B);
          case SHORT -> List.of(FunctionExprent.FunctionType.D2I, FunctionExprent.FunctionType.I2S);
          case INT -> List.of(FunctionExprent.FunctionType.D2I);
          case LONG -> List.of(FunctionExprent.FunctionType.D2L);
          case FLOAT -> List.of(FunctionExprent.FunctionType.D2F);
          case DOUBLE -> List.of();
          case CHAR -> List.of(FunctionExprent.FunctionType.D2I, FunctionExprent.FunctionType.I2C);
          default -> throw new UnsupportedOperationException("Unknown primitive type");
        };
        default -> throw new UnsupportedOperationException("Unknown primitive type");
      };
      for (FunctionExprent.FunctionType funcType : funcTypes) {
        result = new FunctionExprent(funcType, operands, bytecodeOffsets);
        operands = new ArrayList<>(1);
        operands.add(result);
      }
    } else {
      VarType castType = new VarType(DataTypeUtils.getDescriptor(castExpression.castType));
      operands.add(new ConstExprent(castType, null, null));
      result = new FunctionExprent(FunctionExprent.FunctionType.CAST, operands, bytecodeOffsets);
    }
  }

  @Override
  public void visitFieldExpression(FieldExpression fieldExpression) {
    if (fieldExpression.fieldName == null) {
      throw new UnsupportedOperationException("Cannot convert wildcard field expression to exprent");
    }
    if (!fieldExpression.isStatic && nonStaticMethodWrapper == null) {
      throw new UnsupportedOperationException("Cannot substitute non-static field without a method wrapper");
    }

    String internalClassName = fieldExpression.className.replace('.', '/');
    StructClass fieldClass = DecompilerContext.getStructContext().getClass(internalClassName);
    StructField field = fieldClass == null ? null : fieldClass.getFields().stream().filter(fd -> fd.getName().equals(fieldExpression.fieldName)).findFirst().orElse(null);
    Exprent instance = fieldExpression.isStatic ? null : new VarExprent(0, new VarType(nonStaticMethodWrapper.methodStruct.getClassQualifiedName(), true), nonStaticMethodWrapper.varproc, bytecodeOffsets);
    FieldDescriptor fieldDesc = FieldDescriptor.parseDescriptor(field == null ? "I" : field.getDescriptor());
    result = new FieldExprent(fieldExpression.fieldName, internalClassName, fieldExpression.isStatic, instance, fieldDesc, bytecodeOffsets);
  }

  @Override
  public void visitLiteralExpression(LiteralExpression literalExpression) {
    ConstExprent constExpr;
    if (literalExpression.literal instanceof Literal.Integer intLiteral) {
      constExpr = new ConstExprent(VarType.VARTYPE_INT, intLiteral.value(), bytecodeOffsets);
      constExpr.setRadix(intLiteral.radix());
    } else if (literalExpression.literal instanceof Literal.Long longLiteral) {
      constExpr = new ConstExprent(VarType.VARTYPE_LONG, longLiteral.value(), bytecodeOffsets);
      constExpr.setRadix(longLiteral.radix());
    } else if (literalExpression.literal instanceof Literal.Float floatLiteral) {
      constExpr = new ConstExprent(VarType.VARTYPE_FLOAT, floatLiteral.value(), bytecodeOffsets);
    } else if (literalExpression.literal instanceof Literal.Double doubleLiteral) {
      constExpr = new ConstExprent(VarType.VARTYPE_DOUBLE, doubleLiteral.value(), bytecodeOffsets);
    } else if (literalExpression.literal instanceof Literal.Character charLiteral) {
      constExpr = new ConstExprent(VarType.VARTYPE_CHAR, (int) charLiteral.value(), bytecodeOffsets);
    } else if (literalExpression.literal instanceof Literal.String stringLiteral) {
      constExpr = new ConstExprent(VarType.VARTYPE_STRING, stringLiteral.value(), bytecodeOffsets);
    } else {
      throw new UnsupportedOperationException("Unknown literal type");
    }
    constExpr.setConstTypeForced(true);
    result = constExpr;
  }

  @Override
  public void visitUnaryExpression(UnaryExpression unaryExpression) {
    List<Exprent> operands = new ArrayList<>(1);
    unaryExpression.operand.accept(this);
    operands.add(result);

    FunctionExprent.FunctionType funcType = switch (unaryExpression.operator) {
      case NEGATE -> FunctionExprent.FunctionType.NEG;
      case BIT_NOT -> FunctionExprent.FunctionType.BIT_NOT;
    };
    result = new FunctionExprent(funcType, operands, bytecodeOffsets);
  }
}
