package pkg;

import java.util.function.IntSupplier;

public class TestScopes {
  public int packageScope() {
    return Constants.INT_CONST_0;
  }

  public int methodScope() {
    return Constants.INT_CONST_1;
  }

  public int methodOutOfScope() {
    return 1;
  }

  public IntSupplier lambdaScope1() {
    return () -> Constants.INT_CONST_1;
  }

  public IntSupplier lambdaScope2() {
    return () -> Constants.INT_CONST_1;
  }

  public IntSupplier anonymousClassScope() {
    return new IntSupplier() {
      @Override
      public int getAsInt() {
        return Constants.INT_CONST_1;
      }
    };
  }

  static class Nested {
    int ownScope() {
      return Constants.INT_CONST_1;
    }

    int outerScope() {
      return Constants.INT_CONST_2;
    }
  }
}
