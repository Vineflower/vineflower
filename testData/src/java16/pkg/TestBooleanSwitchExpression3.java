package pkg;

public class TestBooleanSwitchExpression3 {
  public void test(String x, int y) {
    if (switch (x) {
      case "BB" -> y < 0;
      case "Aa" -> y > 0;
      default -> y == 0;
    }) {
      System.out.println("Nice");
    }
  }

  public void test1(String x, int y) {
    if (switch (x) {
      case "BB" -> y < 0;
      case "Aa" -> y > 0;
      default -> y == 0;
    }) {
      System.out.println("Nice");
    } else {
      System.out.println("Sad");
    }

    System.out.println("Done");
  }
}
