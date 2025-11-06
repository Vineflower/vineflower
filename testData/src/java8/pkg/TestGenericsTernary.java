package pkg;

import java.util.ArrayList;
import java.util.List;

public class TestGenericsTernary<T> {
  public T[] ts;
  public List<T> list = new ArrayList<>();

  public T test(int i) {
    T t = list.get(i);
    return accept(t) ? t : null;
  }

  public T get(int i) {
    return i > 0 && i < 10 ? ts[i] : null;
  }

  public boolean accept(T t) {
    return t != null;
  }
}
