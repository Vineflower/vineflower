package pkg;

public class TestCatchMulti {
  public void test() {
    try {
      System.out.println("test0 try");
    } catch (IllegalStateException e) {
      System.out.println("test0 catch 0");
    } catch (IllegalArgumentException e) {
      System.out.println("test0 catch 1");
    }
  }
}
