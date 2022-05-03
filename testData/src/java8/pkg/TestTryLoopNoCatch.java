package pkg;

public class TestTryLoopNoCatch {
  public void test(String[] s) {
    boolean b = false;
    for (int i = 0; i < s.length; i++) {
      try {
        b = method(s[i]);
        break;
      } catch (Exception e) {

      }
    }

    System.out.println(b);
  }

  private boolean method(String s) throws Exception {
    if (s.length() > 20) {
      throw new Exception();
    }

    return s.length() > 10;
  }
}
