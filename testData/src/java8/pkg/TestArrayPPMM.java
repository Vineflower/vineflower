package pkg;

public class TestArrayPPMM {
  public void test(int[] array, int i) {
    System.out.println(array[++i]);
  }

  public void test1(int[] array, int i) {
    accept(array[++i], array[++i]);
  }

  public void test2(int[] array, int i) {
    accept(array[i], array[++i]);
  }

  public void test3(int[] array, int i) {
    accept(array[++i], array[i]);
  }

  ///////////

  public void test4(int[] array, int i) {
    System.out.println(array[i++]);
  }

  public void test5(int[] array, int i) {
    accept(array[i++], array[i++]);
  }

  public void test6(int[] array, int i) {
    accept(array[i], array[i++]);
  }

  public void test7(int[] array, int i) {
    accept(array[i++], array[i]);
  }

  public void test8(int[] array, int i) {
    accept(array[i++], array[++i]);
  }

  public void test9(int[] array, int i) {
    array[++i] = i;
  }

  public void test10(int i) {
    getArray()[++i] = i;
  }

  private void accept(int i, int j) {

  }

  private int[] getArray() {
    return new int[10];
  }
}
