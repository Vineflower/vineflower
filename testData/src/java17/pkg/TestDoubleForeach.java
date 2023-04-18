package pkg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TestDoubleForeach {
  private Set<String> one = new HashSet<>();
  private Set<String> two = new HashSet<>();

  public void myMethod() {
    Iterator oneIterator = this.one.iterator();
    Iterator twoIterator = this.two.iterator();

    String myString;

    while (oneIterator.hasNext()) {
      myString = (String)oneIterator.next();
    }

    while (twoIterator.hasNext()) {
      myString = (String)twoIterator.next();
    }
  }

  public void myMethod2() {
    String[] oneArr = this.one.toArray(String[]::new);
    String[] twoArr = this.two.toArray(String[]::new);

    String myString;

    String[] x = oneArr;
    String[] x2 = twoArr;
    int l = x.length;
    int l2 = x2.length;

    for (int i = 0; i < l; i++) {
      myString = x[i];
    }

    for (int i = 0; i < l2; i++) {
      myString = x2[i];
    }
  }
}
