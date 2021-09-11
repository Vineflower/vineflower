package pkg;

public class TestAssignmentSwitchExpression1 {
  public void test(int x) {
    String month = switch (x) {
      case 1 -> "January";
      case 2 -> "February";
      case 3 -> "March";
      case 4 -> "April";
      case 5 -> "May";
      case 6 -> "June";
      case 7 -> "July";
      case 8 -> "August";
      case 9 -> "September";
      case 10 -> "October";
      case 11 -> "November";
      case 12 -> "December";
      default -> throw new IllegalStateException("Unexpected value: " + x);
    };
    System.out.println(month);
  }
}
