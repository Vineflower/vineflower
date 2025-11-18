package pkg;

import java.util.function.Supplier;

public class TestWhileLambda {
    public void test() {
        Object o = new Object();
        while (o != null) {
            Object o2 = new Object();
            Supplier<Object> s = () -> o2;
        }
    }
}
