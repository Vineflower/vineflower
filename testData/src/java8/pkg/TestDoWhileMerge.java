package pkg;

public class TestDoWhileMerge {
  // merging if conditions
  public void test(boolean b, int j) {
    for (int i = 0; i < j; i++) {
      do {
        System.out.println(i);
        i++;

        if (i == 30) {
          return;
        }
      } while (b && i < 40);

      System.out.println("test");
    }

    System.out.println("after");
  }

  public void test1(boolean b, int j) {
    for (int i = 0; i < j; i++) {
      System.out.println(1);

      do {
        System.out.println(i);
        i++;

        if (i == 30) {
          return;
        }
      } while (b && i < 40);
    }

    System.out.println("after");
  }

  public void test2(boolean b, int j) {
    label24:
    for(int i = 0; i < j; ++i) {
      System.out.println(1);

      while(true) {
        System.out.println(i);
        if (++i == 30) {
          return;
        }

        if (!b || i >= 40) {
          continue label24;
        }

        System.out.println(j);
      }
    }
  }
}
