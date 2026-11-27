package pkg;

public class TestSwitchUninlining {
  public void test(int i) {
    switch (i) {
      case Constants.INT_CONST_0:
        System.out.println("Zero");
        break;
      case Constants.INT_CONST_1:
        System.out.println("One");
        break;
    }
  }
}
