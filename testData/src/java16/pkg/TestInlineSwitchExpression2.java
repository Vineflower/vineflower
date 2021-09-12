package pkg;

import ext.Direction;

public class TestInlineSwitchExpression2 {
  public void test(Direction direction) {
    int a = 1;
    int x = Integer.hashCode(switch (direction) {
      case NORTH:
        a |= direction.ordinal();
      case SOUTH:
        a += 12;
      case EAST:
        a *= 8;
      case WEST:
        a ^= 128;
      case UP:
        a /= 5;
      default:
        yield a;
    });
    System.out.println(x);
  }
}
