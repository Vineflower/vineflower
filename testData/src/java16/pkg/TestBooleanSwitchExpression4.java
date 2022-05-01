package pkg;

public class TestBooleanSwitchExpression4 {
  public void test(int x, int y, int z) {
    if (switch (x) {
      case 0 -> y < 0;
      case 1 -> switch (y) {
        case 0 -> true;
        case 1 -> false;
        case 2 -> {
          while (z > 0) {
            z -= x /= 2;
            x += y;
            y += z;

            if (y % z == 0) {
              yield x < 100;
            }
          }

          yield z == 0;
        }
        default -> y == z;
      };
      default -> y == 0;
    }) {
      System.out.println("Nice");
    }
  }

  public void test1(int x, int y, int z) {
    if (switch (x) {
      case 0 -> y < 0;
      case 1 -> switch (y) {
        case 0 -> true;
        case 1 -> false;
        case 2 -> {
          while (z > 0) {
            z -= x /= 2;
            x += y;
            y += z;

            if (y % z == 0) {
              yield x < 100;
            }
          }

          yield z == 0;
        }
        default -> y == z;
      };
      default -> y == 0;
    }) {
      System.out.println("Nice");
    } else {
      System.out.println("Sad");
    }

    System.out.println("Done");
  }
}
