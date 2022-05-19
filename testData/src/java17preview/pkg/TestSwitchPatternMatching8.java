package pkg;

public class TestSwitchPatternMatching8 {
  static String test(String s) {
    return switch (s) {
      case "hi" -> "hi";
      case "bye" -> "bye";
      case null, default -> "oh";
    };
  }
}
