package pkg;

public class TestTernaryCall {
    public void test(boolean a, boolean b, boolean c) {
        System.out.println((b ? c : a) || (c ? a : b));
    }
}
