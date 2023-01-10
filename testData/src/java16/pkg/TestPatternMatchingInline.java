package pkg;

public class TestPatternMatchingInline {
  public void test(Object o) {
    accept(o, o instanceof String s && s.length() > 5);
  }

  public void test2(Object o) {
    accept(o, o instanceof Boolean b && b);
  }

  private void accept(Object o, boolean b) {

  }
}
