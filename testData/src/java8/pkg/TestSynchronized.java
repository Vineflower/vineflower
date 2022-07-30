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

  public void test4_1() {
    Object o;
    synchronized (o = 1) {
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test4_2() {
    Object o;
    synchronized (o = 1.0) {
      o = 1.0;
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test4_3() {
    Object o;
    synchronized (o = 1.0f) {
      o = 1.0f;
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test4_4() {
    Object o;
    synchronized (o = true) {
      o = true;
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test4_5() {
    Object o;
    synchronized (o = 1L) {
      o = 1L;
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test11(int i) {
    switch (i) {
      case 0:
        synchronized (this) {
          break;
        }
      case 1:
        synchronized (this) {
          System.out.println(1);
          break;
        }
      case 2:
        System.out.println(2);
        synchronized (this) {
          break;
        }
      default:
        System.out.println(0);
    }
  }

  public void test13() {
    Object o;
    synchronized (o = "") {
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test14() {
    Object o;
    synchronized (o = "hi") {
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test15() {
    String o;
    synchronized (o = "hi") {
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test16() {
    String o = "a";
    synchronized ("test") {
      System.out.println(o);
    }
    System.out.println(o);
  }

  public void test17() {
    synchronized (TestSynchronized.class) {
      System.out.println("test");
    }
  }
}
