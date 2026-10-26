package pkg;

public class TestNarrowedCounter {
  static char toChar(byte[] b, int pos) {
    return (char) ((b[pos] & 255) | (b[pos + 1] & 255) << 8);
  }

  static int scan(byte[] data) {
    int pos = 0;
    int count = 0;
    while (pos < data.length - 1) {
      char c = toChar(data, pos);
      pos += 2;
      if (c == 1) {
        count++;
      }
    }
    return count;
  }
}
