package pkg;

public class TestArrayNullAccess {
    public int test() {
        byte[] array = new byte[4];
        int n = array.length;
        array = null;
        return array[3];
    }
}
