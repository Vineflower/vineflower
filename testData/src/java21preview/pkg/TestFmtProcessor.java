package pkg;

import static java.util.FormatProcessor.FMT;

public class TestFmtProcessor {
  public void test() {
    String s = "Hello";
    int i = 42;
    double d = 3.14159;

    System.out.println(FMT."Text: %s\{s} %4d\{i} %.2f\{d}");
  }
}
