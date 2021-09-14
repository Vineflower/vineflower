package pkg;

import java.util.List;

public class TestIterationOverGenericsWithoutLvt1 {
  public void test(List<Object> a) {
    for (Object c : a) {
      System.out.println(c.hashCode());
//      System.out.println(c);
    }
  }
}
