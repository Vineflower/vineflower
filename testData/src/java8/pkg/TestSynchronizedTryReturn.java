package pkg;

public class TestSynchronizedTryReturn {
  public int test(String s) {
    synchronized (this) {
      try {
        return Integer.parseInt(s);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public int test1(String s) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
