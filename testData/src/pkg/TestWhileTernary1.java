package pkg;

public class TestWhileTernary1 {
    public int test(boolean condition, int a, int b) {
        if (a > 0) {
            while (condition ? a < b : b > a) {
                System.out.println(a * b);
                a++;
            }

            return 1;
        } else {
            return 0;
        }
    }
}
