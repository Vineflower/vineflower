package pkg;

import java.util.Arrays;

public class TestArrayArg {
  public void in(Object[] in) {
    Arrays.toString(in);
  }

  public void test(String... data) {
    in(data);
    Arrays.toString(data);
  }
}
