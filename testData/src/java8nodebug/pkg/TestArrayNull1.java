package pkg;

public class TestArrayNull1 {
    public int test() {
        byte[] array = new byte[4];
        int n = array.length;
        array = null;
        return n;
    }
}
