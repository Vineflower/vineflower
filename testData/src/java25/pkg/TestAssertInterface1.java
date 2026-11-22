package pkg;

public interface TestAssertInterface1 {
  void empty();

  default void test(int x) {
    assert x > 10;
  }
}
