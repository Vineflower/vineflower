package pkg;

public class TestNestedFor {
    public int test(int x) {
        int out;

        for (int j = out = x; j < 20; ++j) {
            for(int i = out = accept(0); i < 10; i++){
                out++;
            }
        }

        return out;
    }
    private int accept(int num) {
        return num;
    }
}
