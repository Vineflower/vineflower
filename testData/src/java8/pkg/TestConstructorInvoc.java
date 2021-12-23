package pkg;

public class TestConstructorInvoc {
  public TestConstructorInvoc() {

  }

  public void m(int i) {
  }

  public void test() {
    new TestConstructorInvoc().m(10);
  }
}
