package pkg;

public class TestArrayFieldAccess {
  private int[] array = new int[10];
  private int index = 1;
  private int value;

  public void test() {
    this.value = this.array[this.index]++;
  }

  public void test1() {
    this.value = ++this.array[this.index];
  }
}
