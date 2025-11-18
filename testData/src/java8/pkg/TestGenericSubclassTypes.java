package pkg;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

public interface TestGenericSubclassTypes<T> {
  public interface Numerical<T> {
    int get(T in);
  }

  public static class Constant<T> implements Numerical<T> {

    @Override
    public int get(T in) {
      return 1;
    }
  }

  Stream<Constant<T>> cons();

  default Iterator<Numerical<T>> get() {
    return cons().map((c -> (Numerical<T>)c)).iterator();
  }

  default Function<? super Constant<T>, ? extends Constant<T>> func() {
    return i -> i;
  }
}
