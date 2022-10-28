package pkg;

public class TestPatternMatchingStatic {
  public void test(Object o) {
    if (o instanceof TestPatternMatchingStatic) {
      ((TestPatternMatchingStatic)o).method();
    }
  }

  public static void method() {

  }
}
