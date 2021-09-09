package pkg;

public interface TestPrivateInterfaceMethod {
  private int foo() {
    return 20;
  }

  default int bar() {
    return this.foo() + 9;
  }
}
