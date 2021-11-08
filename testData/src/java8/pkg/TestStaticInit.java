package pkg;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TestStaticInit {
  static final Supplier<TestStaticInit> X = () -> Inner.Y;
  static final TestStaticInit Y = null;

  static class Inner {
    static final TestStaticInit Y = null;
  }
}
