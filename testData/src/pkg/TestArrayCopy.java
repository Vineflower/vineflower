package pkg;

public class TestArrayCopy {
    public void test(int[] a) {
        int[] b = a;
        int len = a.length;
        int[] c = new int[len];
        int i = 0;
        while ((i += c[i]) < len) {
            System.arraycopy(c, i, a, len, len - i);
        }
    }
}
