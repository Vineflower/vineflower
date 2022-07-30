package pkg;

public class TestSynchronizeNull {
  public void test() {
    Object o = new Object();
    synchronized (o = null) {
        System.out.println("Hi");
    }
  }

  public void test1() {
    Object o;
    synchronized (o = null) {
      System.out.println("Hi");
    }
  }

  public void test2() {
    Object o;
    synchronized (o = null) {
      o = null;
      System.out.println("Hi");
    }
  }
}
