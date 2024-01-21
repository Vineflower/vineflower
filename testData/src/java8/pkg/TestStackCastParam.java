package pkg;

public class TestStackCastParam {
  public int x;
  public int y;

  public void test(String s) {
    get().b.accept(s, x, y);
  }

  public static A get() {
    return new A();
  }

  public static class A {
    B b = new B();
  }

  public static class B {
    void accept(String s, float x, float y) {

    }
  }
}
