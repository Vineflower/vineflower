package pkg;

public class TestSwitchPatternMatchingReturn2 {
  public int test(Object o) {
    return switch (o) {
      case Integer i && i > 100 -> -i;
      case Integer i -> i;
      case String s -> s.length();
      default -> 0;
    };
  }
}
