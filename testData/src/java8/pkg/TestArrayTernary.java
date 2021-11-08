package pkg;

public class TestArrayTernary {
    public void test(boolean x) {
        int[] a = new int[]{1, 2, 3, 4};
        int[] b = new int[]{4, 3, 2, 1};

        (x ? a : b)[0] = x ? 33 : (a[x ? 3 : 2] == b[3] ? 2 : 4);
        (x ? a : b)[1] = (x ? b : a)[0] + (x ? b : a)[0];
    }
}
