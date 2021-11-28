package pkg;

public class TestSwitchFinally {
  public void test(int i) {
    try {
      System.out.println(1);
    } finally {
      System.out.println("finally");

      switch (i) {
        case 0:
          System.out.println("0");
          break;
      }

      System.out.println("b");
    }
  }

  public void test1(int i) {
    try {
      System.out.println(1);
    } finally {
      System.out.println("finally");

      switch (i) {
        case 0:
          System.out.println("0");
          break;
        case 1:
          System.out.println("1");
          break;
      }

      System.out.println("b");
    }
  }

  public void test2(int i) {
    try {
      System.out.println(1);
    } finally {
      System.out.println("finally");

      switch (i) {
        default:
          System.out.println("default");
      }

      System.out.println("b");
    }
  }

  public int test3(int i) {
    label: {
      try {
        System.out.println(1);
      } finally {
        System.out.println("finally");

        switch (i) {
          case 0:
            System.out.println("0");
            break;
          case 1:
            System.out.println("1");
            break label;
          default:
            System.out.println("Default");
        }

        System.out.println("b");

      }

      System.out.println("d");
      return 1;
    }

    System.out.println("c");
    return 0;
  }
}
