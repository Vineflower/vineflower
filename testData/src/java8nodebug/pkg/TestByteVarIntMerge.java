package pkg;

public class TestByteVarIntMerge {
  static byte[] bytes = new byte[4];

  public static int test(int[] box) {
    int value;
    if (box[0] < bytes.length) {
      value = bytes[box[0]];
    } else {
      value = 15;
    }

    switch ((byte)value) {
      case 2:
        value = box[0];
        return value + 6;
      default:
        return 0;
    }
  }
}
