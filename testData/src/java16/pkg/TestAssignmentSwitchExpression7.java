package pkg;

public class TestAssignmentSwitchExpression7 {
  public void test(int x) {
    String str = switch (x) {
      case 1 -> "1";
      case 2 -> "2";
      default -> "3";
    };
    System.out.println(str);
  }
}
