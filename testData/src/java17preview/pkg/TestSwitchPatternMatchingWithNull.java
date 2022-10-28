package pkg;

public class TestSwitchPatternMatchingWithNull {
  public void test(Object o) {
    switch (o) {
      case null, Integer i -> System.out.println(i);
      case String s -> System.out.println(s);
      default -> System.out.println("Default");
    }
  }

  public void test2(Object o) {
    switch (o) {
      case Integer i, null -> System.out.println(i);
      case String s -> System.out.println(s);
      default -> System.out.println("Default");
    }
  }
}
