package pkg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestIfTernary2 {
  public boolean test(Object a1, Object a2, Object b1, Object b2) {
    if (a1 == null ? b1 == null : b2.equals(b1)) {
      if (a2 == null ? b2 == null : b1.equals(a1)) {
        return true;
      }
    }

    return false;
  }

  public String test1(Object a1, Object a2, Object b1, Object b2) {
    if (a1 == null ? b1 == null : b2 == null) {
      if (a2 == null ? b2 == null : b1 == null) {
        return "1";
      }

      return "2";
    }

    return "3";
  }

  public void test2(Object a1, Object a2, Object b1, Object b2) {
    if (a1 == null ? b1 == null : b2 == null) {
      if (a2 == null ? b2 == null : b1 == null) {
        System.out.println(1);
      }

      System.out.println(2);
    }

    System.out.println(3);
  }

  public String test3(Object a1, Object a2, Object b1, Object b2) {
    if (a1 == null ? b1 == null : b2 == null) {
      System.out.println(2);

      while (a1 == a2) {
        a1 = a2.toString();
      }

      if (b2 == null) {
        System.out.println("hello");
      }
    }

    return "3";
  }
}
