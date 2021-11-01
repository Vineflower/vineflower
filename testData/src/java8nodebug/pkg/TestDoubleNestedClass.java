package pkg;

import java.util.function.Supplier;

public abstract class TestDoubleNestedClass {
  abstract Object test();

  private static final TestDoubleNestedClass INNER1 = new TestDoubleNestedClass() {
    private int x = 5;
    @Override
    Object test() {
      class Local {
        int getX() {
          return x;
        }
      }
      return new Local();
    }
  };

  private static final TestDoubleNestedClass INNER2 = new TestDoubleNestedClass() {
    @Override
    Object test() {
      return new Object() {};
    }
  };

  static class Child1 {
    int x = 5;

    Supplier<TestDoubleNestedClass> foo(int y) {
      int z = 10;
      return () -> new TestDoubleNestedClass() {
        @Override
        Object test() {
          int xy = x + y;
          int yz = y + z;
          return x + xy + yz;
        }
      };
    }
  }
}
