package pkg;

public class TestSynchronizedThrow {
  public void test() {
    synchronized (this) {
      throw new RuntimeException();
    }
  }
}
