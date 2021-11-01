package pkg;

public class TestNestedTernaryAssign {
  public void test(boolean b1, boolean b2, boolean b3) {
    Object o = b1 ? (b2 ? "3" : 3) : (b3 ? "4" : 4);
  }

  public void test2(boolean b1, boolean b2, boolean b3) {
    String s = b1 ? (b2 ? "3" : "33") : (b3 ? "4" : "44");
  }

  public void test3(boolean b1, boolean b2, int a, int b) {
    int c = b1 ? ((a > (b2 ? 3 : b)) ? a : b) : b;

    System.out.println(c);
  }

  public void test4(boolean b1, int a, int b) {
    accept((b1 ? a > b : a < b) ? 0 : 16);
  }

  public void test5(boolean b1, boolean b2, int a, int b) {
    accept((b1 ? a > b : a < b) ? 0 : 16, (b2 ? a > b : a < b) ? 0 : 16);
  }

  private static void accept(int i) {

  }

  private static void accept(int i, int j) {

  }
}
