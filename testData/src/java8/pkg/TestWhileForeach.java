package pkg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestWhileForeach {
  private final List<Object> objects = new ArrayList<>();

  public Object test() {
    Iterator<Object> it = itr();
    while (it.hasNext()) {
      Object o = it.next();

      while (o != null) {
        for (Object o2 : objects) {
          if (o2 != null) {
            return o2;
          }

          o.notify();
        }
        o = o.toString();
      }
    }

    return null;
  }

  private static Iterator<Object> itr() {
    return null;
  }
}
