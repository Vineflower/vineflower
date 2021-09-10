package pkg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class TestDuplicateLocals {
  public static final Function<Object, Predicate<Object>> A = a -> b -> true;
  private int i = 42;

  public void test1(List<List<Object>> a) {
    System.out.println(a);
    a.forEach(b -> {
      List<Object> c = b; // increase the lvt index
      System.out.println(b);
      b.forEach(d -> System.out.println(c));
    });
  }

  public static void test2(List<List<Object>> a) {
    System.out.println(a);
    a.forEach(b -> {
      System.out.println(b);
      b.forEach(c -> System.out.println(c));
    });
  }

  public void test3(List<List<Object>> a) {
    System.out.println(a);
    a.forEach(b -> {
      int c = b.size();
      System.out.println(b);
      b.forEach(d -> System.out.println(c));
    });
  }

  public void test4(Map<String, List<Object>> a) {
    System.out.println(a);
    a.forEach((b, c) -> {
      System.out.println(b);
      a.forEach((d, e) -> System.out.println(b));
    });
  }

  public static void test5(Optional<Object> a) {
    a.ifPresent(b -> System.out.println(b));
  }

  public void test6(Optional<Object> a) {
    a.ifPresent(b -> System.out.println(i + " " + b));
  }

  public static Integer test7(int key) {
    return new HashMap<Integer, Integer>().computeIfAbsent(key, k -> k + 1);
  }

  public class Inner {
    public Integer test7(int key) {
      return new HashMap<Integer, Integer>().computeIfAbsent(key, k -> k + i);
    }
  }

  interface Inner2 {
    Inner2 A = a -> b -> true;

    Predicate<Object> getPredicate(Object o);
  }
}
