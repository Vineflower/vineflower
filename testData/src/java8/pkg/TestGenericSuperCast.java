package pkg;

public class TestGenericSuperCast {
  public class Inner<T> {
    public Class<? super T> get() {
      return null;
    }
  }
  public <T> Class<T> test(Inner<T> inner) {
    Class<T> t = (Class<T>) inner.get();
    return (Class<T>) inner.get();
  }
}
