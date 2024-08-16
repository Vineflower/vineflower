package pkg;

public class TestCatchLoopAfterCatch {
  public void test() {
    Exception e = new Exception();
    try {
      System.out.println("test0 try");
    } catch (Exception e2) {
      e = e2;
      System.out.println("test0 catch");
    }

    while (e.hashCode() > 0) {
      e = new Exception();
    }
  }
}
