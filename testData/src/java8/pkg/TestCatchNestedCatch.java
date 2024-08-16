package pkg;

public class TestCatchNestedCatch {
  public void test() {
    try {
      System.out.println("test0 try");
    } catch (Exception e) {
      System.out.println("test0 catch");

      try {
        System.out.println("test1 try");
      } catch (Exception e2) {
        System.out.println("test1 catch");
      }
    }
  }
}
