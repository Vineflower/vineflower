package pkg;

public class TestSwitchLoop {
  public void test(int i) {
    while (i > 0) {
      i--;

      switch (i) {
        case 0:
          System.out.println("0");
          continue;
        case 1:
          System.out.println("1");
          continue;
        case 2:
          System.out.println("2");
          continue;
      }

      System.out.println("after");
      if (i == 4) {
        break;
      }
    }
  }

  public int test2(int i) {
    loop:
    for (int a = 0; i > a; a++) {
      i--;

      switch (i) {
        case 0:
          System.out.println("0");
          break;
        case 1:
          System.out.println("1");
          return 1;
        case 2:
          System.out.println("2");
          return 2;
        case 3:
          System.out.println("3");
          break loop;
        case 4:
          System.out.println("4");
          break loop;
      }

      System.out.println("after");
    }

    return 0;
  }

  public int test3(int i) {
    loop:
    for (int a = 0; i > a; a++) {
      i--;

      switch (i) {
        case 0:
          System.out.println("0");
          break;
        case 1:
          System.out.println("1");
          return 1;
        case 2:
          System.out.println("2");
          return 2;
        case 3:
          System.out.println("3");
          break loop;
        case 4:
          System.out.println("4");
          break loop;
      }

      System.out.println("after");
    }

    System.out.println("after2");

    return 0;
  }

  public void test4(int i) {
    for (int a = 0; i > a; a++) {
      i--;

      switch (i) {
        case 0:
          System.out.println("0");
          if (a == 0) {
            continue;
          }

          break;
        case 1:
          System.out.println("1");
      }

      System.out.println("after");
    }

    System.out.println("after2");
  }

  public void test5(int i) {
    loop:
    for (int a = 0; i > a; a++) {
      i--;

      switch (i) {
        case 0:
          System.out.println("0");
          for (int i1 = 0; i1 < 5; i1++) {
            switch (i1) {
              case 0:
                System.out.println(0);
                break;
              case 1:
                System.out.println(1);
                break loop;
              case 2:
                return;
            }
          }

          break;
        case 1:
          System.out.println("1");
      }

      System.out.println("after");
    }

    System.out.println("after2");
  }

  public void test6() {
    loop:
    for (int i = 0; i < 10; i++) {

      switch (i) {
        case 0:
          System.out.println("0");
          for (int i1 = 0; i1 < 5; i1++) {
            switch (i1) {
              case 1:
                System.out.println(1);
                break loop;
            }
          }

          break;
      }

      System.out.println("after");
    }

    System.out.println("after2");
  }

  public void test7() {
    loop:
    for (int i = 0; i < 10; i++) {
      for (int i1 = 0; i1 < 5; i1++) {
        switch (i1) {
          case 1:
            System.out.println(1);
            break loop;
        }
      }

      System.out.println("after");
    }

    System.out.println("after2");
  }

  public void test8(int i) {
    switch (i) {
      case 0:
        label: {
          for (int j = 0; j < 10; j++) {
            if (j == 3) {
              break label;
            }
          }

          System.out.println(0);
        }
        System.out.println("after");
      case 1:
        System.out.println(1);
    }

    System.out.println("after2");
  }

  public void test9(int i) {
    switch (i) {
      case 0:
        label: {
          for (int j = 0; j < 10; j++) {
            if (j == 3) {
              break label;
            }
          }

          System.out.println(0);
          break;
        }
        System.out.println("after");
      case 1:
        System.out.println(1);
    }

    System.out.println("after2");
  }

  public void test10(int i) {
    switch (i) {
      case 0:
        label: {
          for (int j = 0; j < 10; j++) {
            if (j == 3) {
              break label;
            }

            if (j == 9) {
              break;
            }
          }

          System.out.println(0);
          break;
        }

        System.out.println("after");
      case 1:
        System.out.println(1);
    }

    System.out.println("after2");
  }
}
