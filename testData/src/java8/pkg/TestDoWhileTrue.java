package pkg;

public class TestDoWhileTrue {
    public void test() {
        int x = 0;
        do {
            x++;
            if (x < 100) {
                continue;
            }

            return;
        } while (true);
    }
}
