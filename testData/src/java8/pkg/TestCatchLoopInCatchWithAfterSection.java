package pkg;

public class TestCatchLoopInCatchWithAfterSection {
  public void test() {
    try {
      System.out.println("test0 try");
    } catch (Exception e) {
      System.out.println("test0 catch");

      while (e.hashCode() > 0) {
        e = new Exception();
      }
    }

    System.out.println("test1 try");
  }
}
