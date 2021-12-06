package pkg;

public class TestQualifiedNew {
  public void foo() {
    TestQualifiedNew instance = new TestQualifiedNew();
    instance.new Inner();
  }

  public class Inner {

  }
}
