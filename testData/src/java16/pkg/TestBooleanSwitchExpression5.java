package pkg;

public class TestBooleanSwitchExpression5 {
  public void test(int x, int y, int z) {
    int a;
    if (switch (x) {
      case 0 -> y < 0 && (a = 5) < z;
      case 1 -> (y > 0 ? (a = 17) : (a = -1)) < z;
      case 2 -> y > 0 ? ((a = 17) < z || 7 > z) : (3 < z && (a = -1) == a);
      default -> (a = y) == 0;
    }) {
      System.out.println(a);
    }
  }

  public void test1(int x, int y, int z) {
    int a;
    if (switch (x) {
      case 0 -> y < 0 && (a = 5) < z;
      case 1 -> (y > 0 ? (a = 17) : (a = -1)) < z;
      case 2 -> y > 0 ? ((a = 17) < z || 7 > z) : (3 < z && (a = -1) == a);
      default -> (a = y) == 0;
    }) {
      System.out.println(a);
    } else {
      System.out.println("Sad");
    }

    System.out.println("Done");
  }
}
