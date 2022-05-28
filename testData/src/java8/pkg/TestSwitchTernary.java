package pkg;

public class TestSwitchTernary {
  public void test(int i, int j, boolean b) {
    switch (b ? i : j) {
      case 1:
        System.out.println("a");
        break;
      case 2:
        System.out.println("4");
      default:
        System.out.println("no");
    }
  }

  public void testString(String i, String j, boolean b) {
    switch (b ? i : j) {
      case "v":
        System.out.println("a");
        break;
      case "a":
        System.out.println("4");
      default:
        System.out.println("no");
    }
  }

  public void testEnum(TestEnum i, TestEnum j, boolean b) {
    switch (b ? i : j) {
      case B:
        System.out.println("a");
        break;
      case A:
        System.out.println("4");
      default:
        System.out.println("no");
    }
  }

  public enum TestEnum {
    A,
    B,
    C
  }
}
