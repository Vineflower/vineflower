package pkg;

public class TestLoopFinally {
  public void test() {
    for (int i = 0; i < 10; i++) {
      try {
        System.out.println(i);
      } finally {
        System.out.println("finally");

        if (i == 5) {
          break;
        }

        System.out.println("finally2");
      }
    }

    System.out.println("after");
  }

  public void test1() {
    for (int i = 0; i < 10; i++) {
      try {
        System.out.println(i);
      } finally {
        System.out.println("finally");

        if (i == 5) {
          System.out.println("continue");
          continue;
        } else if (i == 4) {
          System.out.println("break");
          break;
        }

        System.out.println("finally2");
      }
    }

    System.out.println("after");
  }

  public void test2() {
    for (int i = 0; i < 10; i++) {
      try {
        System.out.println(i);
      } finally {
        System.out.println("finally");

        if (i == 5) {
          break;
        }

        continue;
      }
    }

    System.out.println("after");
  }
}
