package pkg;

public class TestRecordPatterns6 {
  sealed interface I {
    record R1(Object o) implements I {};
    record R2(int i) implements I {};
    record R3(String s) implements I {};
  }

  public Object test1(I in) {
    return switch (in) {
      case I.R1(Object o) -> o;
      case I.R2(int i) -> i;
      case I.R3(String s) -> s;
    };
  }

  public String test2(I in) {
    return switch (in) {
      case I.R1(String s) -> s;
      case I.R3(String s) -> s;
      default -> throw new IllegalStateException();
    };
  }
}
