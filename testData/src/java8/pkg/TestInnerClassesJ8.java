package pkg;

public class TestInnerClassesJ8 {
  class TestInner {
    private final int x;
    private final long z;
    private final String v;

    public TestInner(int x, long z, String v) {

      this.x = x;
      this.z = z;
      this.v = v;
    }
  }

  public void test() {
    new TestInner(10, 20, "hello!");
  }
}
