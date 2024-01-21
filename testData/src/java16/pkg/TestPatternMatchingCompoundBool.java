package pkg;

public class TestPatternMatchingCompoundBool {
  public void test(boolean bl, Object o) {
    if (bl || o instanceof Boolean b && b) {
      System.out.println("true");
    }

    System.out.println("false");
  }
}
