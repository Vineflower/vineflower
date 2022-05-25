package pkg;

public class TestSwitchPatternMatching11 {
  static String test(String s) {
    return switch (s) {
      case "hi" -> "hi";
      case "bye" -> "bye";
      case null -> null;
      case default -> "oh";
    };
  }
}
