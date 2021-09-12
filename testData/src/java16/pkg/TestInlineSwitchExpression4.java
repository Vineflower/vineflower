package pkg;

public class TestInlineSwitchExpression4 {
  public String test(int i) {
    return i > 0 ? switch (i) { default -> "1"; } : switch (i) { default -> "2"; };
  }
}
