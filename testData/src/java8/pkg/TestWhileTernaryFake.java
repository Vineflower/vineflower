package pkg;

public class TestWhileTernaryFake {
    public void test2(boolean condition, int a, int b) {
        while(true) {
            if (condition) {
                if (a >= b) {
                    break;
                }
            } else if (b <= a) {
                break;
            }

            System.out.println(a * b);
            a++;
        }
    }
}
