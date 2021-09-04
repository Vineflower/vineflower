package pkg;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TestGenericCasts {
  public void test1(Consumer<String> c) {
    c.accept(null);
  }

  public void test2(Function<String, List<String>> f) {
    String s = "123abc";
    f.apply(s).remove(s);
    List<String> l = f.apply(s.toUpperCase());
  }

  public void test3(List<List<String>> l) {
    for (int i = l.size() - 1; i >= 0; --i) {
      for (String s : l.get(i)) {
        System.out.println(s);
      }
    }
  }

  public void test4(Collection<String> c) {
    if (c instanceof List) {
      ((List<String>) c).sort(String::compareTo);
    }
    System.out.println(c);
  }
}
