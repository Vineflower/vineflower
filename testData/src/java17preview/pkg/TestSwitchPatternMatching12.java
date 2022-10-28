package pkg;

import ext.Direction;

public class TestSwitchPatternMatching12 {
  static int testTriangle(boolean a, Direction l, Direction r) {
    return switch (a ? l : r) {
      case NORTH, SOUTH, WEST -> 0;
      case EAST, UP -> 1;
      case DOWN, null, default -> -1;
    };
  }
}
