package pkg;

public class TestSwitchPatternMatching7 {
  static String test(Object s) {
    return switch (s) {
      case null, default -> "everything";
    };
  }
}
