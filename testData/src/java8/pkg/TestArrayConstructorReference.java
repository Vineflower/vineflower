package pkg;

import java.util.function.IntFunction;

class TestArrayConstructorReference {
  static final IntFunction<int[]> INT = int[]::new;
  static final IntFunction<String[]> STRING = String[]::new;
  static final IntFunction<String[][]> STRING_ARRAY = String[][]::new;
}
