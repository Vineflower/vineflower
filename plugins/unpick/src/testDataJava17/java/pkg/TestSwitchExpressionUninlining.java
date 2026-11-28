package pkg;

public class TestSwitchExpressionUninlining {
  private static final int CONST_0 = 0;
  private static final int CONST_1 = 1;
  private static final int CONST = 257;

  public int test1(int i) {
    return switch (i) {
      case 0 -> CONST_0;
      case 1 -> CONST_1;
      default -> {
        System.out.println("Block");
        yield CONST;
      }
    };
  }

  public int test2(int i) {
    return switch (i) {
      case CONST_0 -> 0;
      case CONST_1, CONST -> 1;
      default -> 2;
    };
  }
}
