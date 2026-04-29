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

  public void test4() {
    int vvv1 = -68;
    vvv1++;
    if (vvv1-- < 110 ? vvv1 < 1773919094 : vvv1 <= 20) {
      return;
    }
    throw new RuntimeException();
  }

  public void test5() {
    int vvv1 = -68;
    vvv1++;
    if (vvv1-- < 110 ? vvv1 < 1773919094 : vvv1 <= 20 || vvv1 != 380) {
      return;
    }
    throw new RuntimeException();
  }
}
