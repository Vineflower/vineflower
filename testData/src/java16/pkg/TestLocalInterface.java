package pkg;

public class TestLocalInterface {
  public void test(int i) {
    @FunctionalInterface
    interface IntToString {
      String apply(int i);
    }

    IntToString intToString = Integer::toString;
  }
}
