package pkg;

public class TestPPMMMath {
  public void test(int i, int j) {
    i++;
    int b = i + j;
    System.out.println(b);
  }

  public void test1(int i, int j) {
    i++;
    int b = i * j;
    System.out.println(b);
  }

  public void test2(int i, int j) {
    i++;
    int b = (i * j) / i;
    System.out.println(b);
  }
}
