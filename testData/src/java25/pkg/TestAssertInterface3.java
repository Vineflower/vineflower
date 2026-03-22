package pkg;

public class TestAssertInterface3 {
  public interface Interface {
    void empty();

    default void test(int x) {
      assert x > 10;
    }
  }

  public int test() {
    return 5;
  }

  static {
    assert new TestAssertInterface3().test() > 4;
  }
}
