package pkg;

public class TestSingleMethod {
  public double field = Math.random();

  static {
    System.out.println("Hello from static block");
  }

  public TestSingleMethod() {
    System.out.println("Hello from constructor");
  }

  public void test() {
    System.out.println("Hello from test");
    new Object() {
      void foo() {
        System.out.println("Hello from anonymous class");
      }
    };

    class Local {
      void foo() {
        System.out.println("Hello from local class");
      }
    }
  }

  public void test(int i) {
    System.out.println("Hello from test with int arg");
  }

  public void test2() {
    System.out.println("Hello from test2");
  }

  static class Inner {
    void foo() {
      System.out.println("Hello from inner class");
    }
  }
}
