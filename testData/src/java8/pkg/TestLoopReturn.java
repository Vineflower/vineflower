package pkg;

public class TestLoopReturn {
  public void test1(boolean a, boolean b) {
    System.out.println(1);
    while (a) {
      while (b) {
        return;
      }
    }

    System.out.println(2);
  }

  public void test1a(boolean a, boolean b) {
    while (a) {
      while (b) {
        return;
      }
    }

    System.out.println(2);
  }

  public void test1b(boolean a, boolean b) {
    while (a) {
      while (b) {
        return;
      }
    }
  }

  public void test2(boolean b) {
    while (true) {
      while (b) {
        return;
      }

      System.out.println(1);
    }
  }

  public void test2a(boolean b) {
    System.out.println(0);

    while (true) {
      while (b) {
        return;
      }

      System.out.println(1);
    }
  }

  public void test2b(boolean b) {
    System.out.println(0);

    while (true) {
      while (b) {
        System.out.println(2);
        return;
      }

      System.out.println(1);
    }
  }

  public void test3() {
    Object var2 = null;
    System.out.println(var2);
    for (short var3 = -10198; var3 > -27558; var3 *= -2616)
    {
      while (true)
      {
        continue;
      }
    }
    System.out.println(var2);
  }
}
