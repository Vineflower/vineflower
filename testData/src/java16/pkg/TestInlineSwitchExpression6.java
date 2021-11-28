package pkg;

public class TestInlineSwitchExpression6 {
  public void test(int i) {
    int j = 0;
    while (j < i) {
      j++;

      i = switch (j) {
        case 1 -> 3;
        default -> {
          label3:
          if (j == 4) {
            break label3;
          }

          yield 2;
        }
      };
    }
  }
}
