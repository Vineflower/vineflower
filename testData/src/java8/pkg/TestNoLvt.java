package pkg;

public class TestNoLvt {
  public void test(int v) {
    int x;
    if (v > 0) {
      x = 5;
    } else {
      x = 2;
    }

    int y;
    if (v > 0) {
      y = 5000;
    } else {
      y = 3000;
    }

    int z;
    if (v > 0) {
      z = 5000;
    } else {
      z = 3000;
    }

    System.out.println(z);
  }
}
