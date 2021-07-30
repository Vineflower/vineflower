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

    public void test4() {
        do {

        } while (true);
    }

    public int testIf(int i) {
        while (true) {
            if (i == 3) {
                return 1;
            }
        }
    }

    public void testSuccessor1() {
        int a = 0;
        while (true) {

        }
    }

    public void testSuccessor2(int i) {
        int a = 0;
        if (i == 0) {
            a = 3;
        }

        while (true) {

        }
    }

    public void testSuccessor3(int i) {
        int a = 0;

        while (i > 0) {
            a--;
            i--;

            while (true) {

            }
        }
    }

    public void testSuccessor4(int i) {
        i += 2;

        do {
            i --;
            while (true) {

            }
        } while (i > 0);
    }
}
