package pkg;

public class TestShiftLoop {
  public static void test(long[] l) {
    long x;

    x = l[0];
    for (int i = 1; i < 2; i++) {
      x <<= 3;
    }

    x = l[4];
  }
}
