package pkg;

public class TestFinallyThrow {
  public void test(boolean b) {
    while (true) {
      try {
        System.out.println(1);
      } finally {
        try {
          if (b) {
            return;
          }
        } catch (Exception e) {
          throw e;
        } finally {
          System.out.println(2);
        }
      }
    }
  }

  public void test1(RuntimeException t) {
    try {
      System.out.println(1);
    } catch (Throwable e) {
      e.printStackTrace();
    } finally {
      throw t;
    }
  }

  // test1 decompiles as this:
  public void test2(RuntimeException t) {
    try {
      System.out.println(1);
      throw t;
    } catch (Throwable e) {
      e.printStackTrace();
    } finally {

    }

    throw t;
  }
}
