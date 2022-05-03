package pkg;

import java.util.function.Supplier;

public class TestTryReturnNoDebug {
  public String test1(Supplier<Boolean> supplier) {
    String n = null;
    try {
      n = supplier.toString();
    } catch (Exception var3) {
      throw new RuntimeException("Catch");
    }

    return process(n);
  }

  private String process(String s) {
    return s;
  }
}
