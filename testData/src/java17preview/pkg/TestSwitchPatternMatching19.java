package pkg;

public class TestSwitchPatternMatching19 {
  static void test(XXX s) {
    switch (s) {
      case X1                    -> System.out.println("x1");
      case A a && a != XXX.X1    -> System.out.println("a");
      case B b && b instanceof C -> System.out.println("b");
      case D d && d == d         -> System.out.println("d");
      case E e && false          -> System.out.println("e");
      case F f && true           -> System.out.println("f");
    }
  }


  interface A {}
  interface B {}
  interface C {}
  interface D {}
  interface E {}
  interface F {}

  enum XXX implements A, B, C, D, E, F {
    X1, X2, X3
  }
}
