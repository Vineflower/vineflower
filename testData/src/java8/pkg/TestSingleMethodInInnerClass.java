package pkg;

public class TestSingleMethodInInnerClass {
  void test() {
    System.out.println("Hello from outer class");
  }

  static class Inner {
    void test() {
      System.out.println("Hello from inner class");
    }

    void test2() {
      System.out.println("Hello from inner class 2");
    }
  }
}
