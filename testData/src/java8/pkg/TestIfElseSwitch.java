package pkg;

public class TestIfElseSwitch {
  public boolean test(boolean a, boolean b, boolean c, int i) {
    if (i == 3 || i == 2) {
      return true;
    } else if (a) {
      return false;
    } else if (i > 200 || i < -200) {
      return false;
    } else if (c && b) {
      return true;
    } else if (i >= 0 && i < 8) {
      if (i - 1 == 5) {
        return true;
      }

      switch (i) {
        case 0:
          return false;
        case 1:
          return true;
        default:
          return i % 2 == 0;
      }
    } else if (i > 100) {
      System.out.println(1);
      return false;
    }

    return false;
  }
}
