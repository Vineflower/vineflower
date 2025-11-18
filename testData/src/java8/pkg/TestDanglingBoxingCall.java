package pkg;

public class TestDanglingBoxingCall {
  public void test(int x) {
    if ((x ^ 126) == 7) {
      Integer.valueOf(0xFFFF);
    } else {
      Boolean.valueOf(false);
    }

    Float.valueOf(0.9f);
  }
}
