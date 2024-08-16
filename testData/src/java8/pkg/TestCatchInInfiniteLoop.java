package pkg;

public class TestCatchInInfiniteLoop {
  public void test(int i) {
    while (true) {
      try {
        System.out.println("test0 try");
      } catch (Exception e) {
        System.out.println("test0 catch");
        if (e.hashCode() == i) {
          break;
        }
        System.out.println("test0 after break");
      }
    }
  }
}
