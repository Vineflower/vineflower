package pkg;

import java.util.function.Consumer;

public class TestGenericSuper<T extends Number> {
  public T t1;
  public class IO<I, O extends I> {
    public O mutate(I in) {
      return (O)in;
    }
  }

  public <U extends T> void test(IO<T, U> io, Consumer<U> consumer) {
    consumer.accept(null);
    U u = io.mutate(this.t1);
    consumer.accept(u);
  }
}
