package pkg;

public class TestDanglingBoxingCall {
  public void test(int x) {
    if ((x ^ 126) == 7) {
      Integer.valueOf(0xFFFF);
    } else {
      Boolean.valueOf(false);
    }

    Float.valueOf(0.9f);
  }

  public static void consume(Integer value) {
    value.intValue();
  }

  public static void consumeWithBox(Object value) {
    ((Integer)value).intValue();
  }

  public static void consumeWithMath(Integer value) {
    ((Integer)(value + 5)).intValue();
  }

  public static void consumeReverse(Integer value) {
    Integer.valueOf(value);
  }

  public static void consumeReverseMath(Integer value) {
    Integer.valueOf(value + 5);
  }

  public static void consumeReverseAndBack(Integer value) {
    Integer.valueOf(value).intValue();
  }
}
