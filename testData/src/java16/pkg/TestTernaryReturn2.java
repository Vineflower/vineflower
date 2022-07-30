package pkg;

public class TestTernaryReturn2 {
  public T test(T in) {
    return new T(get(in.toString()), (new T("test" + (b(in) ? in.test() : "1"))).test());
  }

  private static boolean b(T in) {
    return in != null;
  }

  private static String get(Object o) {
    return "hi" + o.toString();
  }

  private class T {
    private final String s;
    private final String s2;

    private T(String s) {
      this.s = s;
      this.s2 = "";
    }
    private T(String s, Object... a) {

      this.s = s;
      this.s2 = a[0].toString();
    }

    private String test () {
      return s + s2;
    }
  }
}
