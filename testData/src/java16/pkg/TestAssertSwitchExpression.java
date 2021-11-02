package pkg;

public class TestAssertSwitchExpression {
  public void test(int i, String s) {
    assert s.equals(switch (i) {
      case 1 -> "1";
      case 2 -> "2";
      default -> "Unknown";
    });
  }
}
