package pkg;

public class TestInstanceStaticInvoke {
  private TestInstanceStaticInvoke inst;

  public void test() {
    new TestInstanceStaticInvoke().method();
  }

  public void test1(TestInstanceStaticInvoke param) {
    param.method();
  }

  public void test2() {
    inst.method();
  }

  public static void method() {

  }
}
