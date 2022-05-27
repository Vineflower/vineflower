package pkg;

public class TestSwitchPatternMatching13 {
  static void test(Shape s) {
    switch (s) {
      case Triangle t && (t.calculateArea() > 150) -> {
        if (t.hashCode() > 0) {
          System.out.println("Larger positive-hash triangle");
        }
        System.out.println("Larger triangle");
      }
      case Triangle t && (t.calculateArea() > 50) ->
        System.out.println("Smaller triangle");
      case Triangle t && (t.calculateArea() > 100) -> {
        System.out.println("Large triangle");
        while (Math.random() > 0.5) {
          System.out.println("Keep going");
        }
      }
      case Triangle t -> {
        if (t.hashCode() > 0) {
          System.out.println("Small positive-hash triangle");
        }
        System.out.println("Small triangle");
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
