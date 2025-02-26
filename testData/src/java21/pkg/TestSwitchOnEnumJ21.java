package pkg;

import ext.TestEnum2;

public class TestSwitchOnEnumJ21 {
  public int test1(TestEnum a) {
    return switch (a) {
      case A -> 1;
      case B -> 2;
      case C -> 3;
    };
  }
  
  public int test2(TestEnum2 a) {
    return switch (a) {
      case A -> 1;
      case B -> 2;
      case C -> 3;
    };
  }
  
  public int test3(TestEnum a) {
    return switch (a) {
      case A -> 1;
      case B -> 2;
      case C -> 3;
      case null -> 4;
    };
  }

  public int test4(TestEnum2 a) {
    return switch (a) {
      case A -> 1;
      case B -> 2;
      case C -> 3;
      case null -> 4;
    };
  }

  public int test5(TestEnum a, boolean b) {
    return switch (a) {
      case A -> 1;
      case B -> 2;
      case C -> {
        if (b) {
          boolean c = true;
          yield 3;
        } else {
          boolean d = true;
          yield 4;
        }
      }
    };
  }

  public int testDefault(TestEnum a) {
    return switch (a) {
      case A -> 1;
      default -> 5;
    };
  }
  
  public int testDefault2(TestEnum2 a) {
    return switch (a) {
      case A -> 1;
      default -> 5;
    };
  }

  public void testStatement(TestEnum a) {
    switch (a) {
      case A:
        System.out.println("A");
        break;
      case B:
        System.out.println("B");
        break;
      case C:
        System.out.println("C");
    }
  }

  public void testStatementDefault(TestEnum a) {
    switch (a) {
      case A:
        System.out.println("A");
        break;
      default:
        System.out.println("C");
    }
  }

  public void testComplexPattern(A a) {
    switch (a) {
      case TestEnum.B -> System.out.println("b");
      case TestEnum others -> System.out.println("enum");
      case A x when x instanceof C -> System.out.println(x.getClass());
      case null -> System.out.println("null");
      default -> System.out.println("default");
    }
  }

  public <T> T testEnumSwitchWithCastYields(TestEnum a, java.util.function.Function<Object, T> wrapper) {
    return switch (a) {
      case A -> wrapper.apply("A");
      case B -> wrapper.apply(10);
      case null -> wrapper.apply("null");
      default -> null;
    };
  }

  interface A {

  }

  public static class C implements A {

  }

  enum TestEnum implements A {
    A,
    B,
    C
  }
}
