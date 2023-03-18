package pkg;

public class TestPrimitiveTypeClasses {
  public static void testWrapper() {
    Class<?> c = Integer.class;
    System.out.println(c);
  }

  public static void testPrimitiveType() {
    Class<?> c = int.class;
    System.out.println(c);
  }
}
