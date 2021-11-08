package pkg;

public class TestIfTernary1 {
  public void test(boolean condition, int a, int b) {
    if (condition ? a < b : b > a) {
      System.out.println(1);
    }
  }
}
