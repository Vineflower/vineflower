package pkg;

public class TestArrayAssign2 {
  public Object test(boolean b, Object[] s) {
    if (b) {
      s = (Object[]) method(s);
    }

    return s;
  }

  public Object test1(boolean b, String[] s) {
    if (b) {
      s = (String[]) method(s);
    }

    return s;
  }

  private Object method(Object[] s) {
    return s[0] = new Object();
  }
}
