package org.vineflower.unpick;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.vineflower.unpick.parser.tree.DataType;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class DataTypeUtils {
  private static final Map<VarType, DataType> DATA_TYPE_BY_VAR_TYPE = Map.of(
    VarType.VARTYPE_BYTE, DataType.BYTE,
    VarType.VARTYPE_SHORT, DataType.SHORT,
    VarType.VARTYPE_INT, DataType.INT,
    VarType.VARTYPE_LONG, DataType.LONG,
    VarType.VARTYPE_FLOAT, DataType.FLOAT,
    VarType.VARTYPE_DOUBLE, DataType.DOUBLE,
    VarType.VARTYPE_CHAR, DataType.CHAR,
    VarType.VARTYPE_STRING, DataType.STRING,
    VarType.VARTYPE_CLASS, DataType.CLASS
  );

  private DataTypeUtils() {
  }

  public static DataType descriptorToDataType(String desc) {
    return switch (desc) {
      case "B" -> DataType.BYTE;
      case "S" -> DataType.SHORT;
      case "I" -> DataType.INT;
      case "J" -> DataType.LONG;
      case "F" -> DataType.FLOAT;
      case "D" -> DataType.DOUBLE;
      case "C" -> DataType.CHAR;
      case "Ljava/lang/String;" -> DataType.STRING;
      case "Ljava/lang/Class;" -> DataType.CLASS;
      default -> null;
    };
  }

  public static String getDescriptor(DataType dataType) {
    return switch (dataType) {
      case BYTE -> "B";
      case SHORT -> "S";
      case INT -> "I";
      case LONG -> "J";
      case FLOAT -> "F";
      case DOUBLE -> "D";
      case CHAR -> "C";
      case STRING -> "Ljava/lang/String;";
      case CLASS -> "Ljava/lang/Class;";
    };
  }

  @Nullable
  @Contract("null -> null; !null -> !null")
  public static DataType getDataType(@Nullable Object value) {
    if (value instanceof Byte) {
      return DataType.BYTE;
    } else if (value instanceof Short) {
      return DataType.SHORT;
    } else if (value instanceof Integer) {
      return DataType.INT;
    } else if (value instanceof Long) {
      return DataType.LONG;
    } else if (value instanceof Float) {
      return DataType.FLOAT;
    } else if (value instanceof Double) {
      return DataType.DOUBLE;
    } else if (value instanceof Character) {
      return DataType.CHAR;
    } else if (value instanceof String) {
      return DataType.STRING;
    } else if (value instanceof ClassLiteral) {
      return DataType.CLASS;
    } else if (value == null) {
      return null;
    } else {
      throw new AssertionError("Invalid constant data type: " + value.getClass().getName());
    }
  }

  @Nullable
  public static Object tryCast(@Nullable Object value, @Nullable DataType dataType) {
    if (dataType == null) {
      return value instanceof String || value instanceof ClassLiteral ? value : null;
    }

    return switch (dataType) {
      case BYTE -> tryCastToNumber(value, Number::byteValue);
      case SHORT -> tryCastToNumber(value, Number::shortValue);
      case INT -> tryCastToNumber(value, Number::intValue);
      case LONG -> tryCastToNumber(value, Number::longValue);
      case FLOAT -> tryCastToNumber(value, Number::floatValue);
      case DOUBLE -> tryCastToNumber(value, Number::doubleValue);
      case CHAR -> tryCastToNumber(value, n -> (char) n.intValue());
      case STRING -> value instanceof String ? value : null;
      case CLASS -> value instanceof ClassLiteral ? value : null;
    };
  }

  @Nullable
  private static Object tryCastToNumber(@Nullable Object value, Function<Number, Object> castFunction) {
    if (value instanceof Character c) {
      return castFunction.apply((int) c);
    } else if (value instanceof Number n) {
      return castFunction.apply(n);
    }
    return null;
  }

  @Nullable
  public static Object tryCastExact(@Nullable Object value, @Nullable DataType dataType) {
    if (dataType == null) {
      return value instanceof String || value instanceof ClassLiteral ? value : null;
    }

    return switch (dataType) {
      case BYTE -> tryCastToDoubleFittingNumberExact(value, Number::byteValue);
      case SHORT -> tryCastToDoubleFittingNumberExact(value, Number::shortValue);
      case INT -> tryCastToDoubleFittingNumberExact(value, Number::intValue);
      case LONG -> {
        if (value instanceof Float f) {
          yield (float) f.longValue() == f ? f.longValue() : null;
        } else if (value instanceof Double d) {
          yield (double) d.longValue() == d ? d.longValue() : null;
        } else if (value instanceof Number n) {
          yield n.longValue();
        } else if (value instanceof Character c) {
          yield (long) c;
        } else {
          yield null;
        }
      }
      case FLOAT -> tryCastToDoubleFittingNumberExact(value, Number::floatValue);
      case DOUBLE -> tryCastToDoubleFittingNumberExact(value, Number::doubleValue);
      case CHAR -> {
        Integer result = tryCastToDoubleFittingNumberExact(value, Number::intValue);
        yield result == null || result < 0 || result > Character.MAX_VALUE ? null : (char) result.intValue();
      }
      case STRING -> value instanceof String ? value : null;
      case CLASS -> value instanceof ClassLiteral ? value : null;
    };
  }

  private static <T extends Number> T tryCastToDoubleFittingNumberExact(@Nullable Object value, Function<Number, T> castFunction) {
    if (value instanceof Character c) {
      T result = castFunction.apply((int) c);
      return result.intValue() == (int) c ? result : null;
    }
    if (value instanceof Number n) {
      T result = castFunction.apply(n);
      double a = result.doubleValue();
      double b = n.doubleValue();
      boolean doublesEqual = Double.isNaN(a) ? Double.isNaN(b) : a == b;
      return doublesEqual ? result : null;
    }
    return null;
  }

  public static boolean isPrimitive(@Nullable DataType dataType) {
    return dataType != DataType.STRING && dataType != DataType.CLASS && dataType != null;
  }

  public static boolean isAssignable(@Nullable DataType lhs, @Nullable DataType rhs) {
    if (lhs == rhs) {
      return true;
    }
    if (lhs == null) {
      return false;
    }
    if (rhs == null) {
      return !isPrimitive(lhs);
    }
    if (!isPrimitive(lhs) || !isPrimitive(rhs)) {
      return false;
    }
    if (lhs == DataType.CHAR) {
      return false;
    }
    if (rhs == DataType.CHAR) {
      return lhs.ordinal() >= DataType.INT.ordinal();
    }
    return lhs.ordinal() >= rhs.ordinal();
  }

  @Nullable
  @Contract("null -> null; !null -> !null")
  public static DataType widenNarrowTypes(@Nullable DataType dataType) {
    if (dataType == null) {
      return null;
    }

    return switch (dataType) {
      case BYTE, SHORT, CHAR -> DataType.INT;
      default -> dataType;
    };
  }

  public static String getTypeName(@Nullable DataType dataType) {
    if (dataType == null) {
      return "null";
    }

    return switch (dataType) {
      case STRING -> "String";
      case CLASS -> "Class";
      default -> dataType.name().toLowerCase(Locale.ROOT);
    };
  }

  @Nullable
  public static DataType fromVarType(VarType varType) {
    return DATA_TYPE_BY_VAR_TYPE.get(varType);
  }
}
