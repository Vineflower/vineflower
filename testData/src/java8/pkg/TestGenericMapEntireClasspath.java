package pkg;

import java.util.HashMap;
import java.util.Map;

public class TestGenericMapEntireClasspath {
  public class Inner<T> {
    T get() {
      return null;
    }
  }

  public Map<String, Inner<?>> field = new HashMap<>();

  public <T extends Number> Inner<T> get(String s) {
    return (Inner<T>) this.field.get(s);
  }

  public <T extends Number> Inner<?> get1(String s) {
    return this.field.get(s);
  }

  public Inner<?> get2(String s) {
    return this.field.get(s);
  }

  public Inner get3(String s) {
    return this.field.get(s);
  }
}
