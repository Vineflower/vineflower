package pkg;

public class TestPatternMatchingInvoke {
  interface I {

  }

  class A implements I {

  }

  class B implements I {

  }

  public I get() {
    return new A();
  }

  public boolean bool() {
    return true;
  }

  public void test(boolean cond) {
    System.out.println("Before");

    if (bool()) {
      System.out.println("Inner");
      I i = get();
      if (cond && i instanceof A) {
        I i2 = get();
        System.out.println(i2);
      }
    }

    System.out.println("After");
  }
}
