package pkg;

import ext.Direction;

public class TestInlineSwitchExpression1 {
  public void test(Direction direction) {
    System.out.println(switch (direction) {
      case NORTH:
        yield Direction.SOUTH;
      case SOUTH:
        yield Direction.NORTH;
      case EAST:
        yield Direction.WEST;
      case WEST:
        yield Direction.EAST;
      case UP:
        yield Direction.DOWN;
      case DOWN:
        yield Direction.UP;
    });
  }
}
