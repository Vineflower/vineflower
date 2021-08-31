package pkg;

public class TestSynchronized {
    public void test1() {
        synchronized (this) {

        }
    }

    public void test2() {
        synchronized (new Object()) {

        }
    }

    public void test3() {
        Object o;
        synchronized (o = new Object()) {
            o = new Object();
            System.out.println(o);
        }
        System.out.println(o);
    }

  public void test4() {
    Object o;
    synchronized (o = 1) {
      o = 1;
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test5(int i) {
    try {
      while (true) {
        synchronized(this) {
          while (i >= i) {
            wait();
          }
        }

        synchronized(this) {
          notifyAll();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
