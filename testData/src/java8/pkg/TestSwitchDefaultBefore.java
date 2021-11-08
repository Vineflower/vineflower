package pkg;

public class TestSwitchDefaultBefore {
  public void test(String s) {
    switch (s) {
      case "a":
        System.out.println(1);
        break;
      case "b":
        System.out.println(2);
        break;
      default:
      case "c":
        System.out.println(3);
        break;
    }
  }

  public void test2(int i) {
    switch (i) {
      case 1:
        System.out.println(1);
        break;
      case 2:
        System.out.println(2);
        break;
      default:
      case 3:
        System.out.println(3);
        break;
    }
  }
}
