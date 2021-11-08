package pkg;

public class TestLocalScopes {
    public void test() {
        {
            int i = 0;
            for (int j = 0; j < 10; j++) {
                i += j;
            }
        }

        {
            int k = 0;
            for (int j = 0; j < 10; j++) {
                k += j;
            }
        }
    }
}
