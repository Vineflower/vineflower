package pkg;

import java.util.function.Supplier;

public class TestTryReturn {
  public boolean test(Supplier<Boolean> supplier) {
    try {
      return supplier.get();
    } catch (Exception var3) {
      throw new RuntimeException(var3);
    }
  }

  public boolean testFinally(Supplier<Boolean> supplier) {
    try {
      return supplier.get();
    } finally {
      System.out.println("Finally");
    }
  }

  public void testFinally1(Supplier<Boolean> supplier) {
    System.out.println("pred");

    try {
      if (supplier.get()) {
        return;
      }
    } finally {
      System.out.println("Finally");
    }

    System.out.println("suc");
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

  public boolean testFinally3(boolean b, boolean c, int a, Supplier<Boolean> supplier) {
    try {
      if (b) {
        return c && supplier.get();
      }

      if (a > 0) {
        return a == 1;
      }

      return supplier.get();
    } finally {
      System.out.println("Finally");
    }
  }

  public boolean testFinally4(Supplier<Boolean> supplier) {
    boolean b = false;
    try {
      b = supplier.get();
    } finally {
      System.out.println("Finally");
    }

    return b;
  }
}
