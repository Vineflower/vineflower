package pkg;

public class TestLoopBreak {
    public void test(int i) {
        while (i > 10) {
            i++;

            if (i == 15) {
                break;
            }

            System.out.println(0);
        }
    }
}
