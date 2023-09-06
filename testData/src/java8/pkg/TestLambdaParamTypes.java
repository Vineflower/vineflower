package pkg;

import java.util.function.Function;

public class TestLambdaParamTypes {
  public void f() {
    Function<String, ?> f1a = x -> x;
    Function<String, ?> f1b = x -> x.length() + 1;
    Function<?, ?> f2a = (String x) -> x;
    Function<?, ?> f2b = (String x) -> x.length() + 1;
    g((String x) -> x);
    g((String x) -> x.length() + 1);
    h((String x) -> x);
    h((String x) -> x.length() + 1);
  }

  public void g(Function<?, ?> fn) {
  }

  public <T> void h(Function<T, ?> fn) {
  }
}
