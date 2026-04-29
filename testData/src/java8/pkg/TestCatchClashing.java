package pkg;

public class TestCatchClashing {
  public void test() {
    try {
      System.out.println("Hello");
    } catch (Error e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
