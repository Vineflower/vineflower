package pkg;

public class TestAssignmentSwitchExpression6 {
  public void test(String directionStr, int i) {
    String axis = directionStr;
    while (i > 0) {
      i--;
      axis = switch (directionStr.toLowerCase()) {
        case "north":
        case "south":
          yield "y";
        case "east":
        case "west":
          yield "x";
        case "up":
        case "down":
          yield "z";
        default:
          throw new IllegalStateException("Unexpected value: " + directionStr);
      };

    }
    System.out.println(axis);
  }
}
