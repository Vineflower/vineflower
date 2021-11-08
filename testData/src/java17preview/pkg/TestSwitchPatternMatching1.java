package pkg;

public class TestSwitchPatternMatching1 {
  public void test(Object o) {
    switch (o) {
      case Integer i -> System.out.println(i);
      case String s -> System.out.println(s);
      default -> System.out.println("Default");
    }
  }
}
