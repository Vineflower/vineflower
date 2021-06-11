package pkg;

public class TestInfiniteLoop {
    public void test() {
        while (true) {

        }
    }

    public int testRet() {
        while (true) {

        }
    }

    public int test2() {
        while (true) {
            while (true) {

            }
        }
    }

    public int test3() {
        while (true) {
            while (true) {
                while (true) {

                }
            }
        }
    }

    public int testIf(int i) {
        while (true) {
            if (i == 3) {
                return 1;
            }
        }
    }
}
