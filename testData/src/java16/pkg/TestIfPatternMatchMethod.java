package pkg;

public class TestIfPatternMatchMethod {
  public void testPatternMatchOk() {
    if (provide() instanceof String s) {
      System.out.println("ok " + s);
    }
  }

  public void testPatternMatchBad() {
    if (provide() instanceof String) {
      String s = (String) provide();
      System.out.println("ok " + s);
    }
  }

  public Object provide() {
    return Math.random() < 0.5 ? "" : 0;
  }
}
