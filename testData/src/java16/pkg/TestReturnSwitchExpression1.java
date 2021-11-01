package pkg;

public class TestReturnSwitchExpression1 {
  public String test(int i) {
    return switch (i) {
      case 1 -> "1";
      case 2 -> "2";
      default -> "Unknown";
    };
  }
}
