package pkg;

public class TestMultiBoolean {
    public void test(boolean a) {
        if (a && a) {
            System.out.println(1);
        }

        if (a || a) {
            System.out.println(2);
        }

        if (a && a && a) {
            System.out.println(3);
        }

        if (a && a && a || a || a) {
            System.out.println(4);
        }
    }
}
