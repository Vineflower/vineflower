package pkg;

public class TestSwitchPatternMatching20 {
  public void test(Object o) {
    switch (o) {
      case Integer i && false -> System.out.println(i);
      case String s -> System.out.println(s);
      default -> System.out.println("Default");
    }
  }
}
