package pkg;

import java.util.Iterator;
import java.util.List;

public class TestForeachVardef {
  public String test(List<String> s) {
    String t = null;

    if (s.size() > 10) {
      for (Iterator<String> iterator = s.iterator(); iterator.hasNext(); ) {
        t = iterator.next();

        System.out.println(t);
      }
    } else {
      t = s.get(0).length() > 20 ? "no" : t;
    }

    return t;
  }

  public int testArray(int[] a) {
    int t = 0;

    if (a.length > 10) {
      int l = a.length;
      for (int i = 0; i < l; i++) {
        t = a[i];

        System.out.println(t);
      }
    } else {
      t = a[0];
    }

    return t;
  }
}
