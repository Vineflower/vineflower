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
    this.value = this.array[this.index]++;

    if (this.value == 2) {
      return;
    }

    System.out.println(this.value);
  }

  public void test3() {
    int i = this.array[this.index];
    this.array[this.index] = i + 1;
    this.value = i;
  }

  public void test4() {
    this.value = this.array[this.index] = this.array[this.index] + 1;
  }

  public void test5() {
    if (this.array[this.index]++ == 3) {
      System.out.println(this.array[this.index]);
    }
  }

  public void test6() {
    if (++this.array[this.index] == 3) {
      System.out.println(this.array[this.index]);
    }
  }
}
