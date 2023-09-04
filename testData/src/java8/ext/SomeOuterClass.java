package ext;

public class SomeOuterClass {

  void hello() {
    System.out.println("world");
  }

  public class SomeInner {
    public void greet() {
      SomeOuterClass.this.hello();
    }
  }
}
