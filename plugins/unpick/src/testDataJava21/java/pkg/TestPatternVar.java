package pkg;

public class TestPatternVar {
  private static final int CONSTANT_VALUE = 1;

  public void test(Object o) {
    if (o instanceof R(int value)) {
      if (value == 1) {
        System.out.println("One");
      }
    }
  }

  private record R(int value) {
  }
}
