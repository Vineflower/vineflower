package pkg;

public class TestSwitchPatternMatchingConstructor1 {
  private TestSwitchPatternMatchingConstructor1(String s) {
    System.out.println(s);
  }

  private TestSwitchPatternMatchingConstructor1(Shape s) {
    this(switch (s) {
      case Triangle t && (t.calculateArea() > 100) -> "Large triangle";
      case Triangle t -> "Small triangle";
      case null -> "null";
      default -> "Non-triangle";
    });
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
