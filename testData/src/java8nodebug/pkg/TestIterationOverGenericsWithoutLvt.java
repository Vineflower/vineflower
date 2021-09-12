package pkg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestIterationOverGenericsWithoutLvt {
  public void test1(List<Object> a) {
    int b = 0;
    for (Object c : a) {
      ++b;
      System.out.println(c.hashCode());
    }
  }

  public void test2(List<? extends Number> a) {
    int b = -1;
    for (Number c : a) {
      if (c.intValue() > b) {
        b = c.intValue();
      }
    }
    System.out.println(b);
  }

  public <T extends List<T>> void test3(List<T> a) {
    for (T b : a) {
      test3(b);
    }
  }

  public <T extends Collection<?>> void test4(Collection<? extends T> a) {
    for (T b : a) {
      for (Object c : b) {
        System.out.println(c);
      }
    }
  }

  public <T extends Comparable<T>> void test5(Iterable<T> a) {
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
