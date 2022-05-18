package pkg;

public class TestTryVarNoDebug {
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

  public int test2(int x, int y) {
    for (int i = 0; i < 10; i++) {
      try {
        x = y + i;
        x = (x += 5000 - i/(7-i)) / y;
      } catch (Throwable t) {

      }
    }

    return x;
  }
}
