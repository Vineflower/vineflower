package pkg;

import java.io.IOException;

public class TestSwitchExpressionMultiple {
  public int test(int i1, int i2) {
    return switch (switch (i2) {
      case 0 -> 1;
      case 1 -> 0;
      default -> 0;
    }) {
      case 0 -> 1;
      case 1 -> 0;
      default -> 0;
    } + switch (i1) {
      case 0 -> switch (i2) {
        case 0 -> 1;
        case 1 -> 0;
        default -> 0;
      };
      case 1 -> 0;
      default -> 0;
    };
  }
}
