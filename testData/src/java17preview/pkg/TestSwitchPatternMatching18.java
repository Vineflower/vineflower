package pkg;

public class TestSwitchPatternMatching18 {
  static void test(Integer o) {
    switch (o) {
      case 42                   -> System.out.println("42");
      // All integers less than 50 *except* 42
      case Integer i && i < 50  -> System.out.println("small");
      case Comparable<Integer> i
        && i.compareTo(17) > 0  -> System.out.println("comparable");
      default                   -> System.out.println("default");
    }
  }
}
