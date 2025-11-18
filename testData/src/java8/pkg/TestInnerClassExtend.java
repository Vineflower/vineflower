package pkg;

public class TestInnerClassExtend {
  public class Inner {
    public Inner(String s) {

    }
  }

  public class Inner2 extends Inner {
    public Inner2(String s) {
      super(s);
    }
  }
}
