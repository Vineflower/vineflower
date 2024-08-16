package pkg;

public class TestCatchInLoop {
  public void test(int i) {
    for (int j = 0; j < i; j++) {
      try {
        System.out.println("test0 try: " + j);
      } catch (Exception e) {
        System.out.println("test0 catch");
        if (e.hashCode() == j) {
          break;
        }
        System.out.println("test0 after break");
      }
    }
  }
}
