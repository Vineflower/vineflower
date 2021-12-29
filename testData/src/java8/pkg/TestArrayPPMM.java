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

  private void accept(int i, int j) {

  }
}
