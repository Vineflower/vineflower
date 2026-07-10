package pkg;

public class TestGroovyTrySplit {
  public void test() {
    Object obj = null;
    try {
      obj = new Object();
    } catch (ArithmeticException ex) {
      if (obj != null) {
        System.out.println("a");
      }

      throwMyException(ex.getMessage());
      return;
    } finally {
      System.out.println("b");
    }
  }

  public void testFlat() {
    Object obj = null;
    b1:
    {
      b2: do {  // groovy only allows labeled breaks if it's inside a loop?
        try {
          try {
            obj = new Object();
          } catch (ArithmeticException ex) {
            if (obj != null) {
              System.out.println("a");
            }

            throwMyException(ex.getMessage());
            break b2;
          }
          break b1;
        } catch (Throwable t) {
          System.out.println("b");
          throw t;
        }
      }while(false)
      System.out.println("b");
      return;
    }
    System.out.println("b");
  }

  public static void throwMyException(String message) {
    throw new RuntimeException(message);
  }
}
