package pkg;

public class TestSwitchPatternMatching10 {
  static void test(String s) {
    switch (s) {
      case "hi" -> System.out.println("hi");
      case "bye" -> System.out.println("bye");
      case null, default -> System.out.println("oh");
    }
  }
}
