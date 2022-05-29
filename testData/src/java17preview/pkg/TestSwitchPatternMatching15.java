package pkg;

public class TestSwitchPatternMatching15 {
  static void test(Object s) {
    switch (s) {
      case Integer i && i > 0 -> {
        System.out.println("positive integer: " + i);
      }
      case Number n && n.hashCode() != 0 -> {
        System.out.println("Normal number: " + n);
      }
      case Integer i -> {
        System.out.println(i);
      }
      case Number n -> {
        System.out.println("Number: " + n);
      }
      default -> {
      }
    }
  }
}
