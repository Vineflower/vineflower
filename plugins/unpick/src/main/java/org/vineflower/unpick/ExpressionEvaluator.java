package org.vineflower.unpick;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.attr.StructConstantValueAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.Literal;
import org.vineflower.unpick.parser.tree.expr.BinaryExpression;
import org.vineflower.unpick.parser.tree.expr.CastExpression;
import org.vineflower.unpick.parser.tree.expr.ExpressionVisitor;
import org.vineflower.unpick.parser.tree.expr.FieldExpression;
import org.vineflower.unpick.parser.tree.expr.LiteralExpression;
import org.vineflower.unpick.parser.tree.expr.UnaryExpression;

import java.util.Objects;

public class ExpressionEvaluator extends ExpressionVisitor {
  public Object result;

  @Override
  public void visitBinaryExpression(BinaryExpression binaryExpression) {
    binaryExpression.lhs.accept(this);
    Object left = result;
    binaryExpression.rhs.accept(this);
    Object right = result;

    DataType leftType = DataTypeUtils.getDataType(left);
    DataType rightType = DataTypeUtils.getDataType(right);
    DataType resultType = getBinaryResultType(leftType, rightType);

    result = switch (binaryExpression.operator) {
      case BIT_OR -> cast(upcastAsLong(left) | upcastAsLong(right), resultType);
      case BIT_XOR -> cast(upcastAsLong(left) ^ upcastAsLong(right), resultType);
      case BIT_AND -> cast(upcastAsLong(left) & upcastAsLong(right), resultType);
      case BIT_SHIFT_LEFT -> cast(upcastAsLong(left) << upcastAsLong(right), resultType);
      case BIT_SHIFT_RIGHT -> cast(upcastAsLong(left) >> upcastAsLong(right), resultType);
      case BIT_SHIFT_RIGHT_UNSIGNED -> cast(upcastAsLongUnsigned(left) >>> upcastAsLong(right), resultType);
      case ADD -> {
        if (left instanceof String) {
          yield left + asString(right);
        } else if (right instanceof String) {
          yield asString(left) + right;
        } else {
          Long leftInteger = upcastAsLongOrNull(left);
          Long rightInteger = upcastAsLongOrNull(right);
          if (leftInteger != null && rightInteger != null) {
            yield cast(leftInteger + rightInteger, resultType);
          } else {
            yield cast(upcastAsDouble(left) + upcastAsDouble(right), resultType);
          }
        }
      }
      case SUBTRACT -> {
        Long leftInteger = upcastAsLongOrNull(left);
        Long rightInteger = upcastAsLongOrNull(right);
        if (leftInteger != null && rightInteger != null) {
          yield cast(leftInteger - rightInteger, resultType);
        } else {
          yield cast(upcastAsDouble(left) - upcastAsDouble(right), resultType);
        }
      }
      case MULTIPLY -> {
        Long leftInteger = upcastAsLongOrNull(left);
        Long rightInteger = upcastAsLongOrNull(right);
        if (leftInteger != null && rightInteger != null) {
          yield cast(leftInteger * rightInteger, resultType);
        } else {
          yield cast(upcastAsDouble(left) * upcastAsDouble(right), resultType);
        }
      }
      case DIVIDE -> {
        Long leftInteger = upcastAsLongOrNull(left);
        Long rightInteger = upcastAsLongOrNull(right);
        if (leftInteger != null && rightInteger != null) {
          if (rightInteger == 0) {
            throw new EvaluationException("Cannot divide " + leftInteger + " by zero");
          }
          yield cast(leftInteger / rightInteger, resultType);
        } else {
          yield cast(upcastAsDouble(left) / upcastAsDouble(right), resultType);
        }
      }
      case MODULO -> {
        Long leftInteger = upcastAsLongOrNull(left);
        Long rightInteger = upcastAsLongOrNull(right);
        if (leftInteger != null && rightInteger != null) {
          if (rightInteger == 0) {
            throw new EvaluationException("Cannot divide " + leftInteger + " by zero");
          }
          yield cast(leftInteger % rightInteger, resultType);
        } else {
          yield cast(upcastAsDouble(left) % upcastAsDouble(right), resultType);
        }
      }
    };
  }

  @Override
  public void visitCastExpression(CastExpression castExpression) {
    castExpression.operand.accept(this);
    result = cast(result, castExpression.castType);
  }

  @Override
  public void visitFieldExpression(FieldExpression fieldExpression) {
    String ownerName = fieldExpression.className.replace('.', '/');
    String fieldName = Objects.requireNonNull(fieldExpression.fieldName, "Cannot resolve wildcard field expression");

    StructClass ownerClass = DecompilerContext.getStructContext().getClass(ownerName);
    if (ownerClass == null) {
      throw new EvaluationException("Cannot resolve class " + ownerName);
    }

    StructField field = null;
    resolveField: do {
      for (StructField candidate : ownerClass.getFields()) {
        if (candidate.getName().equals(fieldName)) {
          field = candidate;
          break resolveField;
        }
      }
      ownerClass = ownerClass.superClass == null ? null : DecompilerContext.getStructContext().getClass(ownerClass.superClass.getString());
    } while (ownerClass != null);
    if (field == null) {
      throw new EvaluationException("Cannot resolve field " + ownerName + "." + fieldName);
    }

    DataType actualFieldType = DataTypeUtils.descriptorToDataType(field.getDescriptor());
    StructConstantValueAttribute constant = field.getAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE);
    if (actualFieldType == null || constant == null) {
      // TODO: scan <init> and <clinit> bytecode for constants that aren't stored in ConstantValue?
      throw new EvaluationException("Cannot resolve field value " + ownerName + "." + fieldName);
    }

    if (fieldExpression.fieldType != null && fieldExpression.fieldType != actualFieldType) {
      throw new EvaluationException("Resolved field " + ownerName + "." + fieldName + " has different type than expected");
    }
    if (fieldExpression.isStatic != field.hasModifier(CodeConstants.ACC_STATIC)) {
      throw new EvaluationException("Staticness of resolved field " + ownerName + "." + fieldName + " does not match expected staticness");
    }

    PrimitiveConstant primitiveConstant = ownerClass.getPool().getPrimitiveConstant(constant.getIndex());
    Object value = primitiveConstant.type == CodeConstants.CONSTANT_Class ? new ClassLiteral(primitiveConstant.getString()) : primitiveConstant.value;
    result = cast(value, actualFieldType);
  }

  @Override
  public void visitLiteralExpression(LiteralExpression literalExpression) {
    Literal literal = literalExpression.literal;
    if (literal instanceof Literal.Integer v) {
      result = v.value();
    } else if (literal instanceof Literal.Long v) {
      result = v.value();
    } else if (literal instanceof Literal.Float v) {
      result = v.value();
    } else if (literal instanceof Literal.Double v) {
      result = v.value();
    } else if (literal instanceof Literal.Character v) {
      result = v.value();
    } else if (literal instanceof Literal.String v) {
      result = v.value();
    } else {
      throw new AssertionError("Invalid literal type: " + literal.getClass().getName());
    }
  }

  @Override
  public void visitUnaryExpression(UnaryExpression unaryExpression) {
    unaryExpression.operand.accept(this);
    DataType resultType = DataTypeUtils.widenNarrowTypes(DataTypeUtils.getDataType(result));
    result = switch (unaryExpression.operator) {
      case NEGATE -> {
        Long integer = upcastAsLongOrNull(result);
        if (integer != null) {
          yield cast(-integer, resultType);
        } else {
          yield cast(-upcastAsDouble(result), resultType);
        }
      }
      case BIT_NOT -> cast(~upcastAsLong(result), resultType);
    };
  }

  private static long upcastAsLong(Object value) {
    Long result = upcastAsLongOrNull(value);
    if (result == null) {
      throw new EvaluationException("Cannot interpret " + asString(value) + " as an integer");
    }
    return result;
  }

  @Nullable
  private static Long upcastAsLongOrNull(Object value) {
    if (!DataTypeUtils.isAssignable(DataType.LONG, DataTypeUtils.getDataType(value))) {
      return null;
    }
    return (Long) cast(value, DataType.LONG);
  }

  private static long upcastAsLongUnsigned(Object value) {
    long result = upcastAsLong(value);
    return value instanceof Long ? result : result & 0xffffffffL;
  }

  private static double upcastAsDouble(Object value) {
    Double result = upcastAsDoubleOrNull(value);
    if (result == null) {
      throw new EvaluationException("Cannot interpret " + asString(value) + " as a number");
    }
    return result;
  }

  @Nullable
  private static Double upcastAsDoubleOrNull(Object value) {
    if (!DataTypeUtils.isAssignable(DataType.DOUBLE, DataTypeUtils.getDataType(value))) {
      return null;
    }
    return (Double) cast(value, DataType.DOUBLE);
  }

  public static DataType getBinaryResultType(DataType type1, DataType type2) {
    if (type1 == DataType.STRING || type2 == DataType.STRING) {
      return DataType.STRING;
    } else if (DataTypeUtils.isPrimitive(type1) && DataTypeUtils.isPrimitive(type2)) {
      return DataTypeUtils.widenNarrowTypes(getCommonSuperType(type1, type2));
    } else {
      // return something arbitrary, the appropriate error will be thrown when trying to convert an operand to a number
      return type1;
    }
  }

  private static String asString(Object value) {
    if (value instanceof ClassLiteral type) {
      if (type.desc().startsWith("[")) {
        return "class " + type.desc().replace('/', '.');
      } else if (type.desc().startsWith("L")) {
        String internalName = type.desc().substring(1, type.desc().length() - 1);
        StructClass resolvedClass = DecompilerContext.getStructContext().getClass(internalName);
        if (resolvedClass != null && resolvedClass.hasModifier(CodeConstants.ACC_INTERFACE)) {
          return "interface " + internalName.replace('/', '.');
        } else {
          return "class " + internalName.replace('/', '.');
        }
      } else {
        return switch (type.desc()) {
          case "Z" -> "boolean";
          case "B" -> "byte";
          case "C" -> "char";
          case "S" -> "short";
          case "I" -> "int";
          case "J" -> "long";
          case "F" -> "float";
          case "D" -> "double";
          case "V" -> "void";
          default -> throw new IllegalStateException("Invalid descriptor: " + type.desc());
        };
      }
    } else {
      return String.valueOf(value);
    }
  }

  private static DataType getCommonSuperType(DataType type1, DataType type2) {
    if (DataTypeUtils.isAssignable(type1, type2)) {
      return type1;
    } else if (DataTypeUtils.isAssignable(type2, type1)) {
      return type2;
    } else if (DataTypeUtils.isPrimitive(type1) && DataTypeUtils.isPrimitive(type2)) {
      // possible if either type is a char
      return DataType.INT;
    } else {
      throw new EvaluationException("Cannot find common super type of " + DataTypeUtils.getTypeName(type1) + " and " + DataTypeUtils.getTypeName(type2));
    }
  }

  @Nullable
  @Contract("null, _ -> null; !null, _ -> !null")
  public static Object cast(@Nullable Object value, @Nullable DataType dataType) {
    if (value == null) {
      if (DataTypeUtils.isPrimitive(dataType)) {
        throw new EvaluationException("Cannot cast null to " + DataTypeUtils.getTypeName(dataType));
      } else {
        return null;
      }
    }

    Object result = DataTypeUtils.tryCast(value, dataType);
    if (result == null) {
      throw new EvaluationException("Cannot cast " + value + " to " + DataTypeUtils.getTypeName(dataType));
    }
    return result;
  }

  public static class EvaluationException extends RuntimeException {
    public EvaluationException(String message) {
      super(message);
    }
  }
}
