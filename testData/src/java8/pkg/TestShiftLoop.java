package pkg;

public class TestShiftLoop {
  public static void test(long[] l) {
    long x;

    x = l[0];
    for (int i = 0; i < 1; i++) {
      x <<= 1;
    }

    x = l[1];
  }
}
