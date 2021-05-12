package pkg;

import java.util.Random;

public class TestWhileCondition {
    public void test1() {
        int i = -10;
        int j = 10;

        while ((i < 0 && j > 0) || (i * j > 3)) {
            i++;
            j--;
        }
    }

    public void test2() {
        int i = -10;
        int j = 10;
        boolean b = false;

        while (((i < 0 && j > 0) || (i * j > 3)) || !b) {
            i++;
            j--;
            b = !b;
        }
    }
}
