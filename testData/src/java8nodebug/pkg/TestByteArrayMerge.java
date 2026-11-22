package pkg;

public class TestByteArrayMerge {
  private static byte[] decode(int size, int step) {
    byte[] pixels = null;
    if (size > 0) {
      pixels = new byte[size];
    } else {
      pixels = new byte[1];
    }

    int value = step & 255;
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte)value;
    }

    return pixels;
  }
}