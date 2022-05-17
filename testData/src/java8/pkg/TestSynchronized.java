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

  public void test6() {
    while (true) {
      synchronized (this) {

      }
    }
  }

  public void test7(int i) {
    synchronized (this) {
      while (i > 0) {
        i--;
        System.out.println(i);
      }
    }
  }

  public void test8() {
    try {
      synchronized (this) {
        notifyAll();
      }
    } finally {
      synchronized (this) {
        notifyAll();
      }
    }
  }

  public void test9(int i) {
    label:
    try {
      System.out.println(0);

      synchronized (this) {
        System.out.println(1);
        if (i > 0) {
          break label;
        }
        System.out.println(2);
      }

      System.out.println(3);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (i > 2) {
      System.out.println("Hello!");
    }
  }

  public void test10(int i) {
    try {
      label: {
        System.out.println(0);
        synchronized(this) {
          System.out.println(1);
          if (i > 0) {
            break label;
          }

          System.out.println(2);
        }

        System.out.println(3);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (i > 2) {
      System.out.println("Hello!");
    }
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

  public void test12(int i) {
    label:
    synchronized (this) {
      System.out.println(1);
      while (i > 0) {
        i--;
        System.out.println(1.5);
        try {
          System.out.println(1.6);
        } finally {
          System.out.println(1.7);
          if (i > 5) {
            break label;
          }
        }
      }

      System.out.println(2);
      if (i > -2) {
        System.out.println(3);
      }
    }

    if (i > 2) {
      System.out.println("Hello!");
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

  public void testLoop(double var1) {
    while (var1 >= 88.29) {
      synchronized (this) {
        while (true)
        {
          while (true)
          {
            long var12 = 399L;
            var1 /= 97.81;
            break;
          }
        }
      }
    }
  }
}
