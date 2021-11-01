package pkg;

public class TestSwitchPatternMatching2 {
  static void testTriangle(Shape s) {
    switch (s) {
      case Triangle t && (t.calculateArea() > 100) ->
        System.out.println("Large triangle");
      case Triangle t ->
        System.out.println("Small triangle");
      case null ->
        System.out.println("null");
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
