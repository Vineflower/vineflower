package pkg;

public class TestDuplicatedFieldRead {
  private static volatile int staticField;
  private int instanceField;

  public static int readStaticOnce() {
    int value;
    return (value = staticField) * value;
  }

  public int readInstanceOnce() {
    int value;
    return (value = instanceField) * value;
  }
}
