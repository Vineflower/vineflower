package pkg;

public class TestAssertPatternMatching {
  public void test(int i) {
    Object o = Integer.toString(i);
    assert o instanceof String s && s.length() > 5;
    System.out.println(o);
  }
}
