package pkg;

public class TestAssertsEnabled {
  private boolean field;
  public void test() {
    boolean enabled = false;
    assert enabled = true;
    System.out.println(enabled);
  }

  public void test1(int x) {
    boolean enabled = false;
    if (x > 5) {
      System.out.println("a");
    } else {
      assert enabled = true;
    }
    System.out.println(enabled);
  }

  public void test2(int x) {
    boolean enabled = false;
    if (x > 5) {
      assert enabled = true;
      System.out.println("a");
    }
    System.out.println(enabled);
  }

  public void test3() {
    boolean enabled = false;
    assert enabled = true : "Test";
    System.out.println(enabled);
  }

  public void test4() {
    boolean enabled = false;
    assert enabled = true : "Test " + enabled;
    System.out.println(enabled);
  }

  public void test5(boolean b) {
    boolean enabled = false;
    assert enabled = b;
    System.out.println(enabled);
  }

  public void test6() {
    boolean enabled = false;
    assert enabled = enabled;
    System.out.println(enabled);
  }

  public void test7() {
    boolean enabled = false;
    String s = "a";
    System.out.println(s);
    assert enabled = true : s = "Test";
    System.out.println(enabled + s);
  }

  public void test8() {
    boolean enabled = false;
    String s = "a";
    System.out.println(s);
    assert enabled : s = "Test";
    System.out.println(enabled + s);
  }

  public void test9() {
    boolean enabled = false;
    String s = "a";
    System.out.println(s);
    assert enabled : s = "Test " + enabled;
    System.out.println(enabled + s);
  }

  public void test10(boolean b) {
    boolean enabled = false;
    assert b && (enabled = true);
    System.out.println(enabled);
  }

  public void test11(boolean b) {
    boolean enabled = false;
    assert (enabled = true) && b;
    System.out.println(enabled);
  }

  public void test12() {
    assert field = true;
    System.out.println(field);
  }
}
