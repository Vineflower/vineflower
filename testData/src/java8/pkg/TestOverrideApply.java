package pkg;

public class TestOverrideApply {
  public class A {
    protected void a() {
      System.out.println("hello");
    }
    private void b() {
      System.out.println("hello");
    }
  }

  public class B extends A {
    protected void a() { }
    public void b() { }
  }
}
