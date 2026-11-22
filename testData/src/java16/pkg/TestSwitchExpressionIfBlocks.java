package pkg;

import java.io.IOException;

public class TestSwitchExpressionIfBlocks {

  public void test(int i1, int i2) throws IOException {
    int r = switch (i1) {
      case 0 -> {
        if (i2 == 0) {
          System.out.println();
          yield 0;
        } else {
          yield i2;
        }
      }
      case 1 -> {
        if (i2 == 0) {
          System.out.println();
          yield 1;
        } else {
          yield i1;
        }
      }
      default -> throw new IllegalArgumentException();
    };
    if (r == 0) {
      throw new IllegalArgumentException();
    }
  }

}
