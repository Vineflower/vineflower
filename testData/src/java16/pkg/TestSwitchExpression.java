package pkg;

import java.util.Random;

public class TestSwitchExpression {
  public void test1(int x) {
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

  public void test2(int x) {
    String a = switch (x) {
      case 1, 3, 5, 7, 9, 11 -> "Odd";
      case 2, 4, 6, 8, 10, 12 -> "Even";
      default -> throw new IllegalStateException("Unexpected value: " + x);
    };
    System.out.println(a);
  }

  public void test3(int x) {
    Random random = switch (x) {
      case 1, 2, 3, 4, 5 -> {
        long seed = System.currentTimeMillis() - x * 1000;
        yield new Random(seed);
      }
      case 6, 7, 8, 9, 10 -> new Random();
      case -1, -2, -3, -4, -5 -> {
        int seed = x >> 2;
        yield new Random(seed);
      }
      default -> throw new IllegalStateException("Unexpected value: " + x);
    };
    System.out.println(random.nextInt());
  }

  public void test4(Direction direction) {
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

  public void test5(String directionStr) {
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

  public void test6(Direction direction) {
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

  public void test7(Direction direction) {
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

  public enum Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST,
    UP,
    DOWN
  }
}
