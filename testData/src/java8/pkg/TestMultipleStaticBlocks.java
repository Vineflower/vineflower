package pkg;

public class TestMultipleStaticBlocks {
  private static int i;

  static {
    byte value = (byte) (Math.random() * 8);
    if (value > 4) {
      i = 1;
    }
  }

  static {
    short value = (short) (Math.random() * 8);
    if (value > 4) {
      i = 2;
    }
  }
}
