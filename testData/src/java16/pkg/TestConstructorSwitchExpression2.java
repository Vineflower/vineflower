package pkg;

public class TestConstructorSwitchExpression2 {
  public TestConstructorSwitchExpression2(int i) {
    this(switch (i){
      default -> null;
    });
  }

  public TestConstructorSwitchExpression2(String s) {

  }
}
