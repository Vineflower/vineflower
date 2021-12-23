package pkg;

public class TestSwitchExprInvoc {
  public void test(int i) {
    String res = (switch (i) {
      case 1 -> "one";
      case 2 -> "two";
      default -> "default";
    }).toLowerCase();
  }
}
