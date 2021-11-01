package pkg;

public class TestPatternMatchingLocalCapture {
  public void test(Object o) {
    if (o instanceof String s) {
      new Object() {
        void test() {
          System.out.println(s);
        }
      };
    }
  }
}
