package pkg;

public class TestTryFinally {
  public void test0() {
    try {
      System.out.println("Hello");
    } finally {
      long l = 5;
    }
  }
}
