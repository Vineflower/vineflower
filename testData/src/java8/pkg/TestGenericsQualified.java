package pkg;

import java.util.Comparator;

public class TestGenericsQualified {
  public Comparator<String> field = Comparator.<String, Integer>comparing(s -> s.length()).thenComparing(i -> i.toString());

  public Comparator<String> method() {
    return Comparator.<String, Integer>comparing(s -> s.length()).thenComparing(i -> i.toString());
  }
}
