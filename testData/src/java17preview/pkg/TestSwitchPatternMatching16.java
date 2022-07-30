package pkg;

public class TestSwitchPatternMatching16 {
  static void test3(Object s) {
    outer:
    {
      inner:
      {
        switch (s) {
          case Integer i && i > 0 -> {
            System.out.println("positive integer: " + i);
            break outer;
          }
          case Number n && n.hashCode() != 0 -> {
            System.out.println("Normal number: " + n);
            break outer;
          }
          case Integer i -> {
            // trick qf into inlining target block
          }
          case Number n -> {
            System.out.println("Number: " + n);
            break outer;
          }
          default -> {
            System.out.println("default");
            break outer;
          }
        }
      }
      if(Math.random() < 0.5) {
        int oh = 0;
        int hello = 3;
        break outer;
      }
      System.out.println("hello");
    }
  }
}
