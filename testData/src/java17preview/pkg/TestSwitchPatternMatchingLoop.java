package pkg;

public class TestSwitchPatternMatchingLoop {
  public void test(Object o) {
    while (true) {
      switch (o) {
        case Integer i -> System.out.println(i);
        case String s -> System.out.println(s);
        default -> System.out.println("Default");
      }
    }
  }
}
