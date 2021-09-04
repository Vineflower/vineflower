package pkg;

public class TestWhileTernary3 {
    public void test(boolean condition, int a, int b) {
        while (condition ? a < b : b > a) {
            System.out.println(a * b);
            a++;
        }

        System.out.println(1);
    }
}
