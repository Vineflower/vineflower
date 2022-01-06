package pkg;

public class TestLocalVariableMerge {
  public boolean a;
  public boolean b;
  public double x;

  public void test(double a, double b, double c) {
    if (this.a) {
      double x = a;
      double y = b;
      double z = c;
      if (this.b) {
        a = this.x;
        b = this.x;
        c = this.x;
      }

      if (x != a) {
        System.out.println("x != a");
      }

      if (y != b) {
        System.out.println("y != b");
      }

      if (z != c) {
        System.out.println("z != c");
      }
    }
  }
}
