package pkg;

import ext.Direction;

public class TestSwitchExpressionNested1 {
  public int test(Direction dir, Direction dir2) {
    return switch (dir) {
      case NORTH -> 1;
      case SOUTH -> 2;
      case EAST -> switch (dir2) {
        case NORTH -> 3;
        case SOUTH -> 4;
        case EAST -> 5;
        default -> -1;
      };
      default -> 0;
    };
  }
}
