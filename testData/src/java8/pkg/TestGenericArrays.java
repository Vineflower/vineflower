package pkg;

public class TestGenericArrays<T extends Number> {
  public final T[] arr;
  public final T[][] multi;

  public TestGenericArrays(int i) {
    arr = (T[]) new Number[i];
    multi = (T[][]) new Number[i][];
  }
}
