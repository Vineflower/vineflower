package pkg;

public class TestIfTernary1J17 {
  public void test0(boolean condition, int a, int b, int c) {
    if (condition ? a < b : b > c) {
      System.out.println(1);
    }
  }

  public void test1(boolean condition, int a, int b, int c) {
    if (condition
      ? (a < b ? a == 0 : b == 0)
      : (b > c)) {
      System.out.println(1);
    }
  }

  public void test2(boolean condition, int a, int b, int c) {
    if (condition
      ? (a < b)
      : (b > c ? b == 15 : a == 15)) {
      System.out.println(1);
    }
  }

  public void test3(boolean condition, int a, int b, int c) {
    if (condition
      ? (a < b ? a == 0 : b == 0)
      : (b > c ? b == 15 : a == 15)) {
      System.out.println(1);
    }
  }

  public void test4(boolean condition, int a, int b, int c) {
    if ((condition ? a + c > b : a < b + c)
      ? a < b
      : b > c) {
      System.out.println(1);
    }
  }

  public void test5(boolean condition, int a, int b, int c) {
    if ((condition ? a + c > b : a < b + c)
      ? (a < b ? a == 0 : b == 0)
      : (b > c)) {
      System.out.println(1);
    }
  }

  public void test6(boolean condition, int a, int b, int c) {
    if ((condition ? a + c > b : a < b + c)
      ? (a < b)
      : (b > c ? b == 15 : a == 15)) {
      System.out.println(1);
    }
  }

  public void test7(boolean condition, int a, int b, int c) {
    if ((condition ? a + c > b : a < b + c)
      ? (a < b ? a == 0 : b == 0)
      : (b > c ? b == 15 : a == 15)) {
      System.out.println(1);
    }
  }

  public void test8(boolean condition, int a, int b, int c) {
    if (a == b || b == c
      ? a != b
      : a > b && b > c
      ? a < b + c && a > 3 * c
      : condition) {
      System.out.println(1);
    }
  }

  public void test8b(boolean condition, int a, int b, int c) {
    if (a == b && b == c
      ? !condition
      : a > b && b > c
      ? a < b + c && a > 3 * c
      : condition) {
      System.out.println(1);
    }
  }
}
