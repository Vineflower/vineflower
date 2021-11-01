package pkg;

public class TestWhileTernary6 {
    public void test(boolean condition, int a, int b) {
      do {
        System.out.println(a);
        b++;

        if (a > 3) {
          a--;
        }

      } while (condition ? a < b : b > a);
    }
}
