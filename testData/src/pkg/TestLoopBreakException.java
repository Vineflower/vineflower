package pkg;

public class TestLoopBreakException {
    public boolean test(int i) {
        while (i > 10) {
            i++;

            if (i == 15) {
                continue;
            }

            System.out.println(0);

            return true;
        }

        if (i > 4) {
            return false;
        }

        throw new RuntimeException();
    }
}
