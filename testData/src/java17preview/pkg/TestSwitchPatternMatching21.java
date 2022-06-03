package pkg;

public class TestSwitchPatternMatching21 {
  public void test1(String it) {
    switch (it) {
      case "" -> System.out.println("nothing");
      case "hi" -> System.out.println("hello");
      case String s && Math.random() > 0
        -> System.out.println(s + "!");
      case String s && Math.random() > 0
        -> System.out.println(s + "!!");
      case String s && s.startsWith("?")
        -> System.out.println(s + "?");
      default -> System.out.println("Default");
    }
  }

  public void test2(String it) {
    switch (it) {
      case "" -> System.out.println("nothing");
      case "hi" -> System.out.println("hello");
      case String s && Math.random() > 0
        -> System.out.println(s + "!");
      case String s && Math.random() > 0
        -> System.out.println(s + "!!");
      case String s // total branch
        -> System.out.println(s + "?");
    }
  }
}
