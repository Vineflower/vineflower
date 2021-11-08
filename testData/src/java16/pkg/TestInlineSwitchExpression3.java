package pkg;

import ext.Direction;

public class TestInlineSwitchExpression3 {
  public void test(Direction direction) {
    System.out.println(switch (direction) {
      case NORTH:
      case EAST:
      case UP:
        yield -1;
      case SOUTH:
      case WEST:
      case DOWN:
        yield 1;
    });
  }
}
