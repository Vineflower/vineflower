package pkg;

public class TestDuplicateAssignmentInSwitchExpr {
  void foo(int bar) {
    int num = switch (bar) {
      case 0 -> num = 1;
      default -> 0;
    };
    System.out.println(num);
  }
}
