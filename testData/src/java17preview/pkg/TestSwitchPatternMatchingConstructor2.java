package pkg;

public class TestSwitchPatternMatchingConstructor2 {
  private TestSwitchPatternMatchingConstructor2(String s) {
    System.out.println(s);
  }

  private TestSwitchPatternMatchingConstructor2(Object s) {
    this(switch (s) {
      case null -> "null";
      default -> "Non-triangle";
    });
  }

  private TestSwitchPatternMatchingConstructor2(Object s, boolean unused) {
    this(switch (s) {
      default -> "Non-triangle";
    });
  }
}
