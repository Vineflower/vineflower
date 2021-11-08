package pkg;

public class TestSwitchOnlyDefault {
  public void test(int i) {
    switch (i) {
      default:
        System.out.println("Test");
    }
  }

  public void test2(int i) {
    switch (i) {
      default:
    }
  }
}
