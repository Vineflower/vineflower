package pkg;

public class TestClassFields {
  private static class Inner {
    private static int staticMutable = 3;
  }

  private static int[] sizes;
  private static String[] names = new String[]{"name1", "name2"};

  private static final int SIZE;

  static {
    sizes = new int[names.length];

    SIZE = Inner.staticMutable;
  }
}