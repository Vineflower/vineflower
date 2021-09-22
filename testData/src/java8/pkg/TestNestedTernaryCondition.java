package pkg;

public class TestNestedTernaryCondition {
  public void test(boolean bl, int a, int b) {
    int i = (bl ? a > b : a < b) ? 0 : 16;
  }
}
