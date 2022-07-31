package pkg;

public class TestSwitchPatternMatching14 {
  static void test(Shape s) {
    switch (s) {
      case Triangle t -> {
        if (t.hashCode() > 0) {
          System.out.println("Larger positive-hash triangle");
        }
        System.out.println("Larger triangle: " + t);
      }
      case null -> {
        if (Math.random() > 0) {
          System.out.println("Lucky null");
        }
      }
      default ->
        System.out.println("Non-triangle");
    }
  }

  private abstract class Shape {
    abstract double calculateArea();
  }

  private class Triangle extends Shape {

    @Override
    double calculateArea() {
      return 0;
    }
  }
}
