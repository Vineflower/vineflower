package pkg;

public class TestTempAssign {
  public void test(double a, double b) {
    double c = a;
    a = b;
    b = c;

    System.out.println(a);
    System.out.println(b);
  }

  // a <--> b
  public void test1(double a, double b, double c) {
    if (a > b) {
      c = a;
      a = b;
      b = c;
    }

    System.out.println(a);
    System.out.println(b);
    System.out.println(c);
  }

  public void test2(double a, double b) {
    double c = a;
    System.out.println(a);
    a = b;
    System.out.println(a);
    b = c;

    System.out.println(a);
    System.out.println(b);
  }
}
