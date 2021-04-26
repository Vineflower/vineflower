package pkg;

public class TestArrays {
    private static final int[] array1 = {1, 2, 3, 4};
    private static int[] array2 = {2, 3, 4, 5};
    private int[] array3 = {3, 4, 5, 6};

    void test() {
        int a = array3[3];
        int[] array = new int[a];
        array[array1[1]] = array2[3];
    }
}
