package pkg;

public class TestDoubleAdd {
  public void testParam(double a, double b) {
    double c = a + b + b;
    System.out.println(c);
  }

  public void testParam1(double a, double b) {
    double c = a + b + b + b;
    System.out.println(c);
  }

  double a;
  double b;
  public void testField() {
    double c = a + b + b;
    System.out.println(c);
  }

  public void testField1() {
    double c = a + b + b + b;
    System.out.println(c);
  }
}
