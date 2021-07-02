package pkg;

import java.util.Random;

public class TestAssignmentInLoop {
    private static final Random RANDOM = new Random();

    void test() {
        int i = 10;

        while ((i -= get()) > 0) {
            System.out.println(i);
        }

        for (int j = 0; j < 10; j++, j += get()) {
            System.out.println(j);
        }

        while ((i = get()) == 0) {
            System.out.println(i);
        }

        for (int j = 0; j < 3; j = get()) {
            System.out.println(j);
        }
    }

    private static int get() {
        return RANDOM.nextInt(3);
    }
}
