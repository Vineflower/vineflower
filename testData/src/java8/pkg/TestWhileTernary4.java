package pkg;

public class TestWhileTernary4 {
    public void test(boolean condition, int a, int b) {
        while (condition ? a < b : b > a) {
        }

        System.out.println(1);
    }
}
