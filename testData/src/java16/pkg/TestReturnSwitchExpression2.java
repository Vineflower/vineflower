package pkg;

public class TestReturnSwitchExpression2 {
  public String test(int i) {
    return switch (switch (i) {
      case 1 -> 2;
      case 2 -> 1;
      default -> 3;
    }) {
      case 1 -> "1";
      case 2 -> "2";
      default -> "Unknown";
    };
  }
}
