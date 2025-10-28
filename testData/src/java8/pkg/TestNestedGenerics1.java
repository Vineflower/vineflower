package pkg;

import java.util.function.Function;

public class TestNestedGenerics1 {
  public static <T, R> R test(T t) {
    Function<T, R> instance = new Function<T, R>() {
      @Override
      public R apply(T t) {
        return null;
      }
    };
    return instance.apply(t);
  }
}
