package pkg;

import java.util.List;
import java.util.Map;

public class TestDuplicateLocals {
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
}
