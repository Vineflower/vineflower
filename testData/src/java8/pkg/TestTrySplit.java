package pkg;

public class TestTrySplit {
  public void test() {
    Object obj = null;
    try {
      obj = new Object();
    } catch (ArithmeticException ex) {
      if (obj != null) {
        System.out.println("a");
      }

      throwMyException(ex.getMessage());
      return;
    } finally {
      System.out.println("b");
    }
  }

  public static void throwMyException(String message) {
    throw new RuntimeException(message);
  }
}
