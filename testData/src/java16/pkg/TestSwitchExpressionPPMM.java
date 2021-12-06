package pkg;

public class TestSwitchExpressionPPMM {
  public int test(int test, int x) {
    System.out.println(x);
    int i = 1 + switch (test) {
      case 1 -> x++;
      case 2 -> ++x;
      case 3 -> x--;
      case 4 -> --x;
      default -> x;
    };

    System.out.println(x);
    return i;
  }
}
