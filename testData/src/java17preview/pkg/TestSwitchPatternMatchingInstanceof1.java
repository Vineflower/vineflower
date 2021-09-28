package pkg;

public class TestSwitchPatternMatchingInstanceof1 {
  public void test(Object o) {
    switch (o) {
      case String s && o instanceof Integer a -> System.out.println(s);
      case Integer i -> System.out.println(i);
      case String s -> System.out.println(s);
      default -> System.out.println("Default");
    }
  }
}
