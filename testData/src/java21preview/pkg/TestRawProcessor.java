package pkg;

import static java.lang.StringTemplate.RAW;

public class TestRawProcessor {
  public void test() {
    String s = "Hello";
    int i = 42;
    Object o = null;

    StringTemplate template = RAW."Text: \{s} \{i} \{o}";
  }
}
