package pkg;

public class TestAssignmentSwitchExpression4 {
  public void test(String directionStr) {
    String axis = switch (directionStr.toLowerCase()) {
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
    System.out.println(axis);
  }
}
