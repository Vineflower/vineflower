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

  // Derived from CFR's ExceptionTestFinally20d, https://github.com/leibnitz27/cfr_tests/blob/master/src_6/org/benf/cfr/tests/ExceptionTestFinally20d.java
  public int test3(int x) {
    do {
      try {
        if (x == 1) {
          return 1;
        }

        System.out.println("Oops");
        if (x == 23) {
          return 1;
        }

        System.out.println("Oops");
        if (x == 25) {
          return 1;
        }
      } finally {
        if (x == 3) {
          break;
        }
      }
    } while (x < 45);

    System.out.print(5);
    return 1;
  }

  // simplified version of test3
  public int test4(int x) {
    do {
      try {
        if (x < 25) {
          return 5;
        }
      } finally {
        if (x > 3) {
          break;
        }
      }
    } while (x < 45);

    return 1;
  }

  public int test5(int x) {
    int var2;
    int var3;
    l:
    {
      do {
        try {
          if (x < 25) {
            var2 = 5;
            break l;
          }
        } finally {
          var3 = x;
          if (x > 3) {
            break;
          }
        }
      } while (x < 45);

      return 1;
    }
    return var2 + var3;
  }
}
