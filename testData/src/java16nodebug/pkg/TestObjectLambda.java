package pkg;

public class TestObjectLambda {
  public void test() {
    Object o = new Object();
    Runnable r = () -> {
      Runnable r2 = () -> {
        System.out.println(o);
      };
    };
  }

  public void test2() {
    Object o = new Object();
    Runnable r = () -> {
      System.out.println(o);
    };
  }
}
