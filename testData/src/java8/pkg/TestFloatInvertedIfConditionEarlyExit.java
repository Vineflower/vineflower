package pkg;

// Note that switching < to >= to invert these cases is incorrect, due to comparison with NaN
public class TestFloatInvertedIfConditionEarlyExit {
  public static void test1(float value) {
    if (value < 0) {
      return;
    }
    System.out.println("Hello world!");
  }

  public static void test2(float value) {
    for (float f = 0; f < 5; f++) {
      if (f < value) {
        continue;
      }
      System.out.println("Hello " + f);
    }
  }
}
