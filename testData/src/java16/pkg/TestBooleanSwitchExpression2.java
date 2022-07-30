package pkg;

public class TestBooleanSwitchExpression2 {
  public void test(String x, int y) {
    if (switch (x) {
      case "a" -> y < 0;
      case "b" -> y > 0;
      default -> y == 0;
    }) {
      System.out.println("Nice");
    }
  }

  public void test1(String x, int y) {
    if (switch (x) {
      case "a" -> y < 0;
      case "b" -> y > 0;
      default -> y == 0;
    }) {
      System.out.println("Nice");
    } else {
      System.out.println("Sad");
    }

    System.out.println("Done");
  }
}
