package pkg;

public class TestGenericCastSuper<T> {
  public T t;

  public TestGenericCastSuper<? extends TestGenericCastSuper<T>> get() {
    return (TestGenericCastSuper<? extends TestGenericCastSuper<T>>) this;
  }

  public class Inner<T> extends TestGenericCastSuper<T> {
    public Inner(T t) {

    }

    @Override
    public Inner<? extends TestGenericCastSuper<T>> get() {
      return (Inner<? extends TestGenericCastSuper<T>>) super.get();
    }
  }
}
