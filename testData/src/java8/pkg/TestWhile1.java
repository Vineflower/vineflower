package pkg;

public class TestWhile1 {
  public void test(double a, double g) {
    while (a > 0) {
      a -= 2;
    }

    double d = g;
    while (d >= 3) {
      d -= 2;
    }

    while (d <= 0) {
      d += 2;
    }
  }
}
