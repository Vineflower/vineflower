package pkg;

public class TestInnerClassGeneric<T extends Comparable> {
  T field;


  public class Inner<T extends Number> {
    private final int i;
    private final T t;

    public Inner(int i, T t) {
      this.i = i;
      this.t = t;
    }
  }
}
