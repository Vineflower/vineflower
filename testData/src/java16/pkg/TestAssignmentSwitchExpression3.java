package pkg;

import java.util.Random;

public class TestAssignmentSwitchExpression3 {
  public void test(int x) {
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
}
