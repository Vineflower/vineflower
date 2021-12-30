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

  public void test2() {
    int i = this.array[this.index];
    this.array[this.index] = i + 1;
    this.value = i;
  }

  public void test3() {
    this.value = this.array[this.index] = this.array[this.index] + 1;
  }
  public void test4() {
    this.value = this.array[this.index]++;

    if (this.value == 2) {
      return;
    }

    System.out.println(this.value);
  }

  public int test5(int i, int j) {
    i = j++ / i;
    return i * j;
  }

  public int test6(int i, int j) {
    int a = i + j;
    int b = i - j;
    int c = i * j;
    int d = i / j;
    return i;
  }

  public void test7(){
    this.test7Get()[0]++;
    this.test7Get()[1] = this.test7Get()[1] + 1;
  }

  private int[] test7Get(){
    return new int[10];
  }

}
