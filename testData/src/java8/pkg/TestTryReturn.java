package pkg;

import java.util.Random;
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

  public boolean testFinally5(Supplier<Boolean> supplier) {
    boolean b = false;
    try {
      b = supplier.get();
    } catch (Exception e) {
      System.out.println("Catch");
      b = supplier.get();
    } finally {
      System.out.println("Finally");
    }

    return b;
  }

  public boolean testFinally6(boolean a, Supplier<Boolean> supplier) {
    boolean b = false;
    try {
      if (a) {
        b = true;
        System.out.println("If");
      }

      b = supplier.get();
    } catch (Exception e) {
      System.out.println("Catch");
      b = supplier.get();
    } finally {
      System.out.println("Finally");
    }

    return b;
  }

  public void testLoopFinally() {
    boolean a = true;

    while (true) {
      try {
        if (a) {
          return;
        }
      } finally {
        System.out.println("Finally");
      }
    }
  }

  public void testParsingFailure() {
    char var1 = 't';
    try {
      if (var1 != 'q') {
        try {
          System.out.println(var1);
        } catch (Exception var6) {
          return;
        } finally {
          return;
        }
      }
    } finally {
      System.out.println(var1);
      return;
    }
  }

  public void testPostdomFailure() {
    // Load bearing useless string- removing this makes qf emit a parsing error???
    String var1;
    System.out.println(1);
    label:
    while (new Random().nextBoolean()) {
      try {
        try {
          System.out.println(2);
        } catch (Exception var9) {
          System.out.println(3);
          return;
        } finally {
          continue label;
        }
      } finally {
        byte var10 = 28;
      }
    }
  }
}
