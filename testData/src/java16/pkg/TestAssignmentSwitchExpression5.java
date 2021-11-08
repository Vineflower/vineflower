package pkg;

public class TestAssignmentSwitchExpression5 {
  public void test(String directionStr, int i) {
    String axis = directionStr;
    while (i > 0) {
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
      i--;
    }
    System.out.println(axis);
  }
}
