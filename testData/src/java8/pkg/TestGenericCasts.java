package pkg;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TestGenericCasts {
  public void test1(Consumer<String> c) {
    c.accept(null);
  }

  public void test2(Function<String, List<String>> f) {
    String s = "123abc";
    f.apply(s).remove(s);
    List<String> l = f.apply(s.toUpperCase());
  }

  public void test3(List<List<String>> l) {
    for (int i = l.size() - 1; i >= 0; --i) {
      for (String s : l.get(i)) {
        System.out.println(s);
      }
    }
  }

  public void test4(Collection<String> c) {
    if (c instanceof List) {
      ((List<String>) c).sort(String::compareTo);
    }
    System.out.println(c);
  }

  final class Holder<T> {

  }

  interface I {

  }

  class C implements I {

  }

  interface Func<I, O> {
    O apply(I in);
  }

  private Func<String, Holder<I>> func;

  public C conv(String s) {
    return (C) getOrThrow(func.apply(s), IllegalArgumentException::new);
  }

  static class Inner<X> {
    private Func<String, Holder<X>> func2;
    private Func<String, Holder<I>> func3;
    public <Y extends X> Y conv2(String s) {
      return (Y) getOrThrow(func2.apply(s), IllegalArgumentException::new);
    }

    public <Y extends I> Y conv3(String s) {
      return (Y) getOrThrow(func2.apply(s), IllegalArgumentException::new);
    }

    public <Y extends I> Y conv4(String s) {
      return (Y) getOrThrow(func3.apply(s), IllegalArgumentException::new);
    }
  }

  public static <T, E extends Throwable> T getOrThrow(Holder<T> holder, Function<String, E> function) throws E {
    throw new RuntimeException("");
  }
}
