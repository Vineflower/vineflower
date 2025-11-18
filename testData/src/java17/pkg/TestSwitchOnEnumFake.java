package pkg;

public class TestSwitchOnEnumFake {
  enum Values {
    A,
    B,
    C,
    D
  }

  public byte[] values = new byte[Values.values().length];

  public int test(Values v) {
    int a = 0;
    int b = 0;
    switch (values[v.ordinal()]) {
      case 1 -> {
        a = 1;
        b = 2;
      }
      case 2 -> {
        a = 2;
        b = 4;
      }
      case 3 -> {
        a = 3;
        b = 6;
      }
      default -> {
        a = 1;
        b = 1;
      }
    }

    return a + b;
  }
}
