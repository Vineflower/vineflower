package pkg;

public class TestLabeledBreaks {
  // Mom can get some goto?
  // We already have goto at home.
  // The goto at home:

  public void test(int a) {
    a1: {
      System.out.println("1");
      if (a == 1) {
        break a1;
      }

      System.out.println("2");
    }
  }

  public void test1(int a) {
    a1: {
      for (int i = 0; i < a; i++) {
        System.out.println("1");
        if (a == 1) {
          break a1;
        }

        if (a == 2) {
          break;
        }

        System.out.println("2");
      }

      System.out.println("3");
    }

    System.out.println("4");
  }

  public void test2(int a) {
    for (int i = 0; i < a; i++) {
      a1: {
        System.out.println("1");
        if (a == 1) {
          break a1;
        }

        if (a == 2) {
          break;
        }

        System.out.println("2");
      }

      System.out.println("3");
    }

    System.out.println("4");
  }
}
