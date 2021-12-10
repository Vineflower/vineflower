package pkg;

public class TestArrayNewAccess {
  public void test() {
    new int[]{0, 1}[2] = 1;
  }

  public void test1() {
    System.out.println(new int[]{0, 1}[2]);
  }

  public void test2() {
    System.out.println(new int[]{0, 1}[2] = 1);
  }
}
