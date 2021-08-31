package pkg;

public class TestMisc {
  // TODO: move to another test class
  public static void a(String s) {
    int l = s.length();
    int x = 0;

    while (x < 4 && x < l) {
      char c = s.charAt(x);
      if (c < 0 || c > 9) {
        throw new IllegalArgumentException();
      }
      x++;
    }
    for (int i = 0; i < 4; ++i) {
      if (l < 0 || x > 255) {
        throw new IllegalArgumentException();
      }
    }
  }

  // TODO: move to another test class
  public static void b(long[] l) {
    long x;

    x = l[0];
    for (int i = 0; i < 1; i++) {
      x <<= 1;
    }

    x = l[1];
  }
}
