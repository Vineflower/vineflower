package pkg;

public class TestCatchNestedTry {
  public void test() {
    try {
      System.out.println("test0 try");

      try {
        System.out.println("test1 try");
      } catch (RuntimeException e) {
        System.out.println("test1 catch");
      }

      System.out.println("test2 try");

      try {
        System.out.println("test3 try");
      } catch (Exception e) {
        System.out.println("test3 catch");
      }

      System.out.println("test4 try");
    } catch (Exception e) {
      System.out.println("test0 catch");
    }
  }
}
