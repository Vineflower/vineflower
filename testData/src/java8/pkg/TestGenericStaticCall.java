package pkg;

public class TestGenericStaticCall<T> {
  public T t;

  public static void test() {
    method(new Object(), null);
  }

  public void test1() {
    method(this, null);
  }

  public static <T> void method(Object o, T t) {

  }
}