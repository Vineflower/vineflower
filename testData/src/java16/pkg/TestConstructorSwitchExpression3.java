package pkg;

import java.util.Random;

public enum TestConstructorSwitchExpression3 {
  T1(switch (get()) {
    case 0 -> 1;
    case 1 -> 2;
    default -> 3;
  }),
  T2(switch (get()) {
    case 0 -> 1;
    case 1 -> 2;
    default -> 3;
  });

  TestConstructorSwitchExpression3(int i) {
    System.out.println(i);
  }

  private static int get() {
    return new Random().nextInt(3);
  }
}
