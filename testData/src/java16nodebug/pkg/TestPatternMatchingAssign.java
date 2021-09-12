package pkg;

public class TestPatternMatchingAssign {
  public void test(Object o) {
    if (o instanceof String s) {
      s = "hello";
      System.out.println(s);
    }
  }
}
