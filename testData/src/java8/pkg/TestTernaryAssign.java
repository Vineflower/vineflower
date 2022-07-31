package pkg;

public class TestTernaryAssign {
  public int test(int i) {
    int a;
    method(a = i > 10 ? i : i + 20);

    return a;
  }

  private void method(Object o) {

  }
}
