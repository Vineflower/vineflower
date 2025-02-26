package pkg;

public class TestSwitchExpressionReturnType {
  public int asInt(Type type) {
    return switch (type) {
      case A -> 6000;
      case B -> 6001;
      case C -> 6002;
      case D -> 6003;
    };
  }

  public char asChar(Type type) {
    return switch (type) {
      case A -> 6000;
      case B -> 6001;
      case C -> 6002;
      case D -> 6003;
    };
  }

  enum Type {
    A,
    B,
    C,
    D
  }
}
