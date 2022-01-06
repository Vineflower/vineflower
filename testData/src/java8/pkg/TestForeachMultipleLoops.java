package pkg;

import java.util.Map;

public class TestForeachMultipleLoops {
  // Generates as multiple loops, continues turn into breaks
  public void test(Object a, Map<Integer, String> map, int i) {
    if (a != null) {
      System.out.println(a);
      return;
    }

    for (Map.Entry<Integer, String> entry : map.entrySet()) {
      String s = entry.getValue();
      if (a == null) {
        s += s;
      } else {
        if ((s != null)) {
          continue;
        }
        s = "hello";
      }

      Object v = entry.getValue();
      if (v == null) {
        if (i == 3) {
          continue;
        }
        System.out.println("if");
      } else {
        System.out.println("else");
      }

      try {
        System.out.println(1);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
