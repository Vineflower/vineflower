package pkg;

public class TestAssignmentSwitchExpression2 {
  public void test(int x) {
    String a = switch (x) {
      case 1, 3, 5, 7, 9, 11 -> "Odd";
      case 2, 4, 6, 8, 10, 12 -> "Even";
      default -> throw new IllegalStateException("Unexpected value: " + x);
    };
    System.out.println(a);
  }
}
