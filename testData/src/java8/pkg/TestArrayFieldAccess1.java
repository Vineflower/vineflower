package pkg;

public class TestArrayFieldAccess1 {
  private int[] array = new int[10];
  private int index = 1;
  private int value;

  public void test() {
    this.value = this.array[this.index + 5]++;
  }

  public void test1() {
    this.value = ++this.array[this.index + 5];
  }

  public void test2() {
    this.value = this.array[this.index + 5]++;

    if (this.value == 2) {
      return;
    }

    System.out.println(this.value);
  }

  public void test3() {
    int i = this.array[this.index + 5];
    this.array[this.index + 5] = i + 1;
    this.value = i;
  }

  public void test4() {
    this.value = this.array[this.index + 5] = this.array[this.index + 5] + 1;
  }

  public void test5() {
    if (this.array[this.index + 5]++ == 3) {
      System.out.println(this.array[this.index + 5]);
    }
  }

  public void test6() {
    if (++this.array[this.index + 5] == 3) {
      System.out.println(this.array[this.index + 5]);
    }
  }

  public void test7() {
    this.value = this.array[this.index | 12]++;
  }
}
