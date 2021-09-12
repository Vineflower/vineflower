package pkg;

public class TestReturnSwitchExpression3 {
  public String test(int i) {
    System.out.println(2);

    return switch (i) {
      case 1 -> "1";
      case 2 -> "2";
      default -> "Unknown";
    };
  }
}
