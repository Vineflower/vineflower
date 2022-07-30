package pkg;

public class TestArrayFieldAccess2 {
  private int[] array = new int[10];
  private int index = 1;
  private int value;

  public void test() {
    this.value = this.array[this.index]++;
  }

  public void test1() {
    this.value = this.array[this.index] += 4;
  }

  public void test2() {
    this.value = this.array[this.index] -= 4;
  }

  public void test3() {
    this.value = this.array[this.index] *= 4;
  }

  public void test4() {
    this.value = this.array[this.index] /= 4;
  }

  public void test5() {
    this.value = this.array[this.index] |= 4;
  }

  public void test6() {
    this.value = this.array[this.index] &= 4;
  }

  public void testPP() {
    this.value = this.array[this.index++]++;
  }

  public void test1PP() {
    this.value = this.array[this.index++] += 4;
  }

  public void test2PP() {
    this.value = this.array[this.index++] -= 4;
  }

  public void test3PP() {
    this.value = this.array[this.index++] *= 4;
  }

  public void test4PP() {
    this.value = this.array[this.index++] /= 4;
  }

  public void test5PP() {
    this.value = this.array[this.index++] |= 4;
  }

  public void test6PP() {
    this.value = this.array[this.index++] &= 4;
  }
}
