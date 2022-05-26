package pkg;

public class TestSynchronizedLoop {
  public void test1(int i) {
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

  public void test2() {
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

  public void testFlatten() {
    long var2 = -151L;
    long var3 = -384L;
    while (var2 == 5) {
      synchronized (this) {
        var3 -= -714L;
      }
    }
  }
}
