package pkg;

import java.util.HashMap;
import java.util.Iterator;

public class TestStaticIterator {
  private static HashMap hashMap = new HashMap();

  private static Iterator iterator() {
    return hashMap.values().iterator();
  }

  private static Iterable iterable() {
    return hashMap.values();
  }

  public static void test() {
    Iterator iter = iterator();
    while (iter.hasNext()) {
      TestStaticIterator attr = (TestStaticIterator) iter.next();
    }
  }

  public static void test1() {
    for (Object o : iterable()) {
      System.out.println(o);
    }
  }
}
