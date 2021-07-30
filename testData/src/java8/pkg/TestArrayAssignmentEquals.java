package pkg;

public class TestArrayAssignmentEquals {
    public int test() {
        int[] a = new int[]{4};
        a[0] *= 2;
        a[0] *= 2;
        a[0] *= 2;
        return a[0] * a[0] * 2;
    }
}
