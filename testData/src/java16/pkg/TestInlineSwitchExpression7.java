package pkg;

public enum TestInlineSwitchExpression7 {
  A,
  B,
  C;

  private int get() {
    return ordinal();
  }

  public int test(TestInlineSwitchExpression7 in) {
    return switch (in) {
      case A -> {
        int a = get();
        yield a + 1;
      }
      case B -> {
        int b = get();
        yield b + 2;
      }
      case C -> {
        int c = get();
        yield c + 3;
      }
    };
  }
}
