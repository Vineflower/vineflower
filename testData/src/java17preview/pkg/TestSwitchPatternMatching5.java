package pkg;

public class TestSwitchPatternMatching5 {
  static void test(Shape s) {
    switch (s) {
      case Triangle t && (t.calculateArea() > 150) ->
        System.out.println("Larger triangle");
      case Triangle t && (t.calculateArea() > 50) ->
        System.out.println("Smaller triangle");
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
