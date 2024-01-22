package pkg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestItrLoop {
  private List list = new ArrayList();

  public void test() {
    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      String s = (String) iterator.next();
      System.out.println(s);
    }
  }

  public void test1() {
    for (Object o : list) {

    }
  }

  public void test2() {
    for (String s : (List<String>) list) {

    }
  }

  public void test3() {
    for (String s : (Iterable<String>) list) {

    }
  }
}
