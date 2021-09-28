package pkg;

public class TestSwitchPatternMatching3 {
  static void test(Object s) {
    switch (s) {
      case null ->
        System.out.println("null");
      default ->
        System.out.println("default");
    }
  }
}
