package pkg;

public class TestSynchronizedTry {
  public void test1() {
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

  public void test2(int i) {
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

  public void test3(int i) {
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

  public void testLabel() {
    String var1 = "Hi!";

    Object var8 = null;
    synchronized (this) {
      // Causes label to not be
      String var9 = "Hi!";

      try {
        if (var1 == null) {
          return;
        } else {
          return;
        }
      } catch (Exception var11) {
      }
    }
    System.out.println(var8);
    return;
  }
}
