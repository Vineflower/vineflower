package pkg;

public class TestWhileTernary7 {
    public void test(boolean condition, int a, int b) {
      for (int i = 0; condition ? a < i : b < i; i++) {
        System.out.println("Test");
      }
    }
}
