package pkg;

public class TestGenericsHierarchy<T> {
  public T field;

  public <V extends T> void test(V v) {
    this.field = v;
  }
}
