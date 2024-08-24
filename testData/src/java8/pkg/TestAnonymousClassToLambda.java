package pkg;

public class TestAnonymousClassToLambda {
  public Object test1(Object o) {
    return new Object() {
      public Runnable getRunnable() {
        return () -> System.out.println(o);
      }
    };
  }
}
