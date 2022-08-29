package pkg;

public class TestArrayNull2 {
    private TestArrayNull2(int[] array) {

    }

    public void test() {
        int[] array = null;
        Object o = new TestArrayNull2(array);
    }
}
