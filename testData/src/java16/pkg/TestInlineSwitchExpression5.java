package pkg;

public class TestInlineSwitchExpression5 {
  public void test(int i) {
    int j = 0;
    while (j < switch (i) {
      case 1 -> 4;
      case 2 -> 8;
      default -> 5;
    }) {
      j++;

      System.out.println("hi");
    }
  }
}
