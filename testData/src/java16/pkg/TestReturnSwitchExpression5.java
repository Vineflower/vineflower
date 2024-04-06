package pkg;


public class TestReturnSwitchExpression5 {
  public String test(int i) {
    return switch (i) {
      case 1 -> "1";
      case 2 -> "2";
      default -> {
        int a = 0;
        throw new RuntimeException();
      }
    };
  }
}
