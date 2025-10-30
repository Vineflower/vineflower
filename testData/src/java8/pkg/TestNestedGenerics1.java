package pkg;

import java.util.function.Function;
import java.util.function.Supplier;

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
  
  public static <T, R> R testInLambda(T t, R r) {
    Supplier<Function<T, R>> functionMaker = () -> new Function<T, R>() {
      @Override
      public R apply(T t) {
        return r;
      }
    };

    return functionMaker.get().apply(t);
  }
}
