package pkg;

public class TestArrayDoWhile {
    public int[] test() {
        int[] array = new int[10];
        int i = 1;
        do {
            array[i - 1] = i;
            array[array.length - i] = i * 4;
            i++;
        } while (i < array.length);

        return array;
    }
}
