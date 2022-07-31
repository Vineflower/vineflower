package pkg;

public class TestInnerClassExtend {
  public class Inner {
    public Inner(String s) {

    }
  }

  public static class Inner2 extends Inner {
    public Inner2(TestInnerClassExtend outer, String s) {
      outer.super(s);
    }
  }
}
