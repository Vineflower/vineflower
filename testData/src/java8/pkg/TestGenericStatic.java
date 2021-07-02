package pkg;

import java.util.HashMap;
import java.util.Map;

public class TestGenericStatic<T extends TestGenericStatic.Generic> {
    private final T generic;

    public TestGenericStatic(T generic) {
        this.generic = generic;
    }

    public static void main() {

    }

    private static <T extends Generic> T call(String name, TestGenericStatic<T> holder) {
        return holder.generic;
    }

    public static class Generic {
        void test() {
            System.out.println("Hi!");
        }
    }
}
