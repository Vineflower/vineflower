package pkg;

public class TestSwitchExpressionFallthrough1 {
  public void test(int i) {
    int j = switch (i) {
      case 1:
        System.out.println(i);
      case 2:
      case 3:
      case 4:
        yield 2;
      default:
        yield 3;
    };

    System.out.println(j);
  }
}
