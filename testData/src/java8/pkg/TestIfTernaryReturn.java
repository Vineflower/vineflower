package pkg;

public class TestIfTernaryReturn {
  public int test(boolean condition, int a, int b) {
    if (condition ? a < b : b > a) {
      return 1;
    }

    return -1;
  }

  public int test1(boolean condition, int a, int b) {
    if (condition) {
      if (a == 4) {
        System.out.println(2);
        return 4;
      }
    } else {
      if (b == 5) {
        System.out.println(4);
        return 3;
      }
    }

    return -1;
  }
}
