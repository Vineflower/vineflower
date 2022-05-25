package pkg;

public class TestBooleanExpressions {
  public int field;

  public TestBooleanExpressions[] array;

  public int test(int i) {
    return i > 0 && i < 10 && this.array[i].field > 0 ? this.array[i].field : -1;
  }

  public int test1() {
    return this.array[0].field;
  }

  public int test2() {
    return this.get().array[0].field;
  }

  public TestBooleanExpressions get() {
    return this;
  }
}
