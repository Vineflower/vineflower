package pkg;

public class TestTernaryCall {
    public void test(boolean a, boolean b, boolean c) {
        System.out.println((b ? c : a) || (c ? a : b));
    }

  public void test2(boolean a, boolean b, boolean c) {
      if (b ? c : a) {
        System.out.println(c);
      }
  }
}
