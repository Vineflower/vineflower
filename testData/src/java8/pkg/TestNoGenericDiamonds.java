package pkg;

public class TestNoGenericDiamonds {
  class I<T> {

  }

  private static I<String> is;

  class Inner<T> {
    public Inner(I<? extends I<T>> i, T t) {

    }
  }

  public void test(String s) {
    method(new Inner(is, s));
  }

  private void method(Inner<String> i) {

  }
}
