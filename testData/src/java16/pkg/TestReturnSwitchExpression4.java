package pkg;

public class TestReturnSwitchExpression4 {
  public String test(int i) {
    return switch (i) {
      case 1 -> "1";
      case 2 -> "2";
      default -> {
        if (i > 0) {
          yield "Unknown";
        } else {
          System.out.println("Negative");
          yield "Negative";
        }
      }
    };
  }
}
