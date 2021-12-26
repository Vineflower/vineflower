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

  public <T> Class<? extends T> test1(Inner<T> inner) {
    Class<? extends T> t = (Class<? extends T>) inner.get();
    return (Class<? extends T>) inner.get();
  }
}
