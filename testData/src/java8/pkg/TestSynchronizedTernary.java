package pkg;

public class TestSynchronizedTernary {
  public void test(boolean bl, Object a, Object b) {
    synchronized (bl ? a : b) {
      System.out.println(a);
    }
  }
}