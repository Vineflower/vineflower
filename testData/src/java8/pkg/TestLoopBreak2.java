package pkg;

public class TestLoopBreak2 {
    public boolean test(int i) {
        while (i > 10) {
            i++;

            if (i == 15) {
                continue;
            }

            System.out.println(0);

            return true;
        }

        return false;
    }
}
