package pkg;

import ext.Direction;

public class TestSwitchPatternMatching6 {
  static int testTriangle(Direction d) {
    return switch (d) {
      case NORTH, SOUTH, WEST -> 0;
      case EAST, UP -> 1;
      case DOWN, null, default -> -1;
    };
  }
}
