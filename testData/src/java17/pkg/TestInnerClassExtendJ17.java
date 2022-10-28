package pkg;

public class TestInnerClassExtendJ17 {
  public class Inner {
    public Inner(String s) {

    }
  }

  public static class Inner2 extends Inner {
    public Inner2(TestInnerClassExtendJ17 outer, String s) {
      outer.super(s);
    }
  }
}
