package pkg;

public class TestShiftAssignmentInCall {
    public void test(int x) {
        System.out.println((x <<= 4) & (x >>= 3) | x & (x >> 2));
    }
}
