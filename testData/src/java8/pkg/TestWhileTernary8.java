package pkg;

public class TestWhileTernary8 {
    public void test(boolean condition, int a, int b) {
        while (condition ? a < b ? a == 3 : b == 4 : b > a) {
            System.out.println(a * b);
            a++;
        }
    }
}
