package pkg;

import java.util.function.IntFunction;

class TestArrayConstructorReference {
  static final IntFunction<int[]> INT_REF = int[]::new;
  static final IntFunction<int[]> INT_SIMPLE_LAMBDA = len -> new int[2 * len];
  static final IntFunction<int[]> INT_LONG_LAMBDA = len -> {
    len *= 2;
    return new int[len];
  };
  static final IntFunction<int[][]> INT_ARRAY_REF = int[][]::new;
  static final IntFunction<int[][]> INT_ARRAY_SIMPLE_LAMBDA = len -> new int[2 * len][];
  static final IntFunction<int[][]> INT_ARRAY_LONG_LAMBDA = len -> {
    len *= 2;
    return new int[len][];
  };
  static final IntFunction<String[]> STRING_REF = String[]::new;
  static final IntFunction<String[]> STRING_SIMPLE_LAMBDA = len -> new String[2 * len];
  static final IntFunction<String[]> STRING_LONG_LAMBDA = len -> {
    len *= 2;
    return new String[len];
  };
  static final IntFunction<String[][]> STRING_ARRAY_REF = String[][]::new;
  static final IntFunction<String[][]> STRING_ARRAY_SIMPLE_LAMBDA = len -> new String[2 * len][];
  static final IntFunction<String[][]> STRING_ARRAY_LONG_LAMBDA = len -> {
    len *= 2;
    return new String[len][];
  };
}
