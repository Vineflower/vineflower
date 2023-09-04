package pkg;

public class TestSingleCaseStrSwitch {
  public String test(String s) {
    return switch (s) {
      case "" -> "foo bar";
      default -> s;
    };
  }
}
