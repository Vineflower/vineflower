package pkg;

public class TestSwitchPatternMatchingJ21 {
  public void test1(Object o) {
    System.out.println(switch (o) {
      case Integer i -> Integer.toString(i);
      case null, default -> "null";
    });
  }
}
