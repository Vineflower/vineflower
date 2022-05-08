package pkg;

public class TestTryVar {
  public int test(int x, int y) {
    for (int i = 0; i < 10; i++) {
      try {
        x = y + i;
        x = 5000 - i;
        x = y / y;
      } catch (Throwable t) {

      }
    }

    return x;
  }
}
