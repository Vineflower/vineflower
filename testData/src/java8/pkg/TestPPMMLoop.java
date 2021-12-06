package pkg;

public class TestPPMMLoop {
  public void test(int a, String s) {
    while (++a > 0) {
      s += "a";
    }
  }

  public void test1(int a, String s) {
    while (a++ > 0) {
      s += "a";
    }
  }

  public void test2(int a, String s) {
    while (--a > 0) {
      s += "a";
    }
  }

  public void test3(int a, String s) {
    while (a-- > 0) {
      s += "a";
    }
  }
}
