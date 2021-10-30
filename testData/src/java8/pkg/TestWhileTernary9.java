package pkg;

public class TestWhileTernary9 {
  public void test(boolean condition, boolean a, boolean b) {
    int i = 0;
    while (condition ? a : b) {
      i++;

      if (i == 8) {
        break;
      }
    }
  }

  public void test1(boolean condition, boolean a, boolean b) {
    if (Math.random() > 0.5) {
      int i = 0;
      while (condition ? a : b) {
        i++;

        if (i == 8) {
          break;
        }
      }
    }
  }

  public void test2(boolean condition, boolean a, boolean b) {
    if (Math.random() > 0.5) {
      int i = 0;
      while (condition ? a : b) {
        i++;

        if (i == 8) {
          break;
        }
      }

      System.out.println("Successor");
    }
  }
}
