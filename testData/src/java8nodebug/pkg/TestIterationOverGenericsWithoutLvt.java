package pkg;

import java.util.Collection;
import java.util.List;

public class TestIterationOverGenericsWithoutLvt {
  public void test1(List<? extends Number> a) {
    int b = -1;
    for (Number c : a) {
      if (c.intValue() > b) {
        b = c.intValue();
      }
    }
    System.out.println(b);
  }

  public <T extends List<T>> void test2(List<T> a) {
    for (T b : a) {
      test2(b);
    }
  }

  public <T extends Collection<?>> void test3(Collection<? extends T> a) {
    for (T b : a) {
      for (Object c : b) {
        System.out.println(c);
      }
    }
  }

  public <T extends Comparable<T>> void test4(Iterable<T> a) {
    T b = null;
    for (T c : a) {
      if (b == null) {
        b = c;
      } else {
        int d = b.compareTo(c);
        b = d >= 0 ? b : c;
      }
    }
    System.out.println(b);
  }
}
