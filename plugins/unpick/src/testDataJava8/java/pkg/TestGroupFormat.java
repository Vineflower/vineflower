package pkg;

public class TestGroupFormat {
  public char testDecimal() {
    return 42;
  }

  public int testBinary() {
    return 0b1010;
  }

  public int testOctal() {
    return 0377;
  }

  public int testHex() {
    return 0xCAFEBABE;
  }

  public int testChar() {
    return 'ඞ';
  }

  public long testBinaryLong() {
    return 0b1010_1010_1010_1010_1010_1010_1010_1010_1010L;
  }

  public long testOctalLong() {
    return 01234567012345L;
  }

  public long testHexLong() {
    return 0xCAFEBABE_DEADBEEFL;
  }

  public int testCharOutOfBounds() {
    return 0x1234_5678;
  }

  public int testFlagResidualFormat() {
    return Constants.INT_FLAG_BIT_2 | 0b1000_0000;
  }
}
