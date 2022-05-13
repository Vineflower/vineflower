package pkg;

import java.util.List;
import java.util.function.Function;

public class TestGenericInput<T> {
  private Inner<T> inner;

  public interface Inner<T> extends Function<List<T>, TestGenericInput<T>> {
  }

  public void test(List<T> list) {
    this.inner.apply(list);
  }
}
