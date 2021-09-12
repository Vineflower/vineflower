package pkg;

import java.util.List;

public class TestIterationOverGenericsWithoutLvt1 {
  public void test(List<Object> a) {
    int b = 0;
    for (Object c : a) {
      ++b;
      System.out.println(c.hashCode());
    }
  }
}
