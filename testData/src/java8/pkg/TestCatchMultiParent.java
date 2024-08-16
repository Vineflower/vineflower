package pkg;

public class TestCatchMultiParent {
  public void test() {
    try {
      System.out.println("test0 try");
    } catch (IllegalStateException e) {
      System.out.println("test0 catch 0");
    } catch (Exception e) {
      System.out.println("test0 catch 1");
    }
  }
}
