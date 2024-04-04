package pkg;

public class TestSwitchOnEnumJ21 {
  public int test1(TestEnum a) {
    return switch (a) {
      case A -> 1;
      case B -> 2;
      case C -> 3;
    };
  }

  public int testDefault(TestEnum a) {
    return switch (a) {
      case A -> 1;
      default -> 5;
    };
  }

  public void testStatement(TestEnum a) {
    switch (a) {
      case A:
        System.out.println("A");
        break;
      case B:
        System.out.println("B");
        break;
      case C:
        System.out.println("C");
    }
  }

  public void testStatementDefault(TestEnum a) {
    switch (a) {
      case A:
        System.out.println("A");
        break;
      default:
        System.out.println("C");
    }
  }

  enum TestEnum {
    A,
    B,
    C
  }
}
