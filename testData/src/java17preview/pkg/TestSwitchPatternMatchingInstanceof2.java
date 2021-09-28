package pkg;

public class TestSwitchPatternMatchingInstanceof2 {
  public void test(Object o) {
    switch (o) {
      case Number n && n instanceof Integer i -> System.out.println(i);
      case Number n -> System.out.println(n);
      case String s -> System.out.println(s);
      default -> System.out.println("Default");
    }
  }
}
