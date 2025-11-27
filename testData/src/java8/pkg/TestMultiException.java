package pkg;

public class TestMultiException {
  public void test(int v) {
    try {
      Thread.sleep(50 / v);
    } catch (InterruptedException | ArithmeticException e) {
      throw new RuntimeException(e);
    }
  }

  public void test2() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException | VirtualMachineError e) {
      throw new RuntimeException(e);
    }
  }
}
