package pkg;

public interface TestSubclassReturnType {
  TestSubclassReturnType get();

  class A implements TestSubclassReturnType {
    public A get() {
      return new A();
    }
  }

  class B extends A {
    @Override
    public B get() {
      return new B();
    }
  }
}