package pkg;

public class TestNestedLambdas {

    public void test() {
        int x = accept(i -> {
            if (i == 0) {
                accept(j -> {
                    if (j == 0) {
                        return accept(k -> i + j);
                    } else {
                        return i * j;
                    }
                });
            }

            return i;
        });

        System.out.println(x);
    }

    private static int accept(Func func) {
        return func.get(0);
    }

    @FunctionalInterface
    private interface Func {
        int get(int i);
    }
}
