package pkg;

public class TestSwitchExprString1 {
  enum Type {
    A, B
  }

  public Type get(String s) {
    if (s == null) {
      return Type.B;
    }

    return switch (s) {
      case "a" -> Type.A;
      default -> Type.B;
    };
  }
}
