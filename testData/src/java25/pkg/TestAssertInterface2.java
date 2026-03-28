package pkg;

public class TestAssertInterface2 {
  public interface Interface {
    void empty();

    default void test(int x) {
      assert x > 10;
    }
  }

  public void test() {
    System.out.println();
  }
}
