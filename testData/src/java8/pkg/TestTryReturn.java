package pkg;

import java.util.function.Supplier;

public class TestTryReturn {
  public boolean test(Supplier<Boolean> supplier) {
    try {
      return supplier.get();
    } catch (Exception var3) {
      System.out.println("Catch");
      return false;
    }
  }

  public boolean testFinally(Supplier<Boolean> supplier) {
    try {
      return supplier.get();
    } finally {
      System.out.println("Finally");
    }
  }

  public boolean testFinally2(Supplier<Boolean> supplier) {
    boolean b;
    try {
      b = supplier.get();
    } finally {
      System.out.println("Finally");
    }

    return b;
  }
}
