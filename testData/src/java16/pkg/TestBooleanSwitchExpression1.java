package pkg;

public class TestBooleanSwitchExpression1 {
  public void test(int x, int y) {
    if (switch (x) {
      case 1 -> y < 0;
      case 2 -> y > 0;
      default -> y == 0;
    }) {
      System.out.println("Nice");
    }
  }

  public void test1(int x, int y) {
    if (switch (x) {
      case 1 -> y < 0;
      case 2 -> y > 0;
      default -> y == 0;
    }) {
      System.out.println("Nice");
    } else {
      System.out.println("Sad");
    }

    System.out.println("Done");
  }
}
