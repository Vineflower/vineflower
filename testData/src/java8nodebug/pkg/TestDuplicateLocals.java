package pkg;

import java.util.List;

public class TestDuplicateLocals {
  public void test1(List<List<Object>> a) {
    System.out.println(a);
    a.forEach(b -> {
      List<Object> c = b;
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
}
