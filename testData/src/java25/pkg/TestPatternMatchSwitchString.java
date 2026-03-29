package pkg;

public class TestPatternMatchSwitchString {
  private String field;

  public void test(String s) {
    switch (s) {
      case "a", "b", "c", "d" -> this.field = s;
      case null, default -> throw new RuntimeException("illegal");
    }
  }
}
