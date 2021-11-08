package pkg;

public class TestSwitchPatternMatchingReturn1 {
  public int test(Object o) {
    return switch (o) {
      case Integer i -> i;
      case String s -> s.length();
      default -> 0;
    };
  }
}
