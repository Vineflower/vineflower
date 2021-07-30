package pkg;

public class TestWhileTernary5 {
  public void test(boolean condition, int a, int b, int c) {
    while (c > 3) {
      c--;
      System.out.println(2);

      while (condition ? a < b : b > a) {
        System.out.println(a * b);
        a++;
      }

      System.out.println(1);
    }
  }
}
