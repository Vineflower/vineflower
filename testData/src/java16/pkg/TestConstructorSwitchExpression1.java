package pkg;

public class TestConstructorSwitchExpression1 {
  public TestConstructorSwitchExpression1(int i) {
    this(switch (i){
      case 1 -> "1";
      case 2 -> "3";
      case 4 -> "5";
      default -> "0";
    });
  }

  public TestConstructorSwitchExpression1(String s) {

  }
}
