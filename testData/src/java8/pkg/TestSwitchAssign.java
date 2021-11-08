package pkg;

public class TestSwitchAssign {
    public void test(int x) {
        int assign = 1;
        switch (x) {
            case 1:
            case 3:
            case 5:
                assign = 3;
                break;
            case 2:
            case 4:
            case 6:
                assign = 4;
                break;
        }

        System.out.println(assign);
    }

  public void test1(String s) {
    int assign;
    switch (s) {
      case "a":
      default:
        assign = 3;
        break;
      case "0":
        assign = -2;
        break;
      case "?":
        assign = 999;
        break;
    }

    System.out.println(assign);
  }
}
