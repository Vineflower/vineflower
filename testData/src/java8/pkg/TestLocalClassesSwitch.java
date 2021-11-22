package pkg;

// Adapted from CFR
public class TestLocalClassesSwitch {
  public void test(int i) {
    switch (i) {
      case 0:
        class LocalClass {
          public void test() {
            System.out.println("test");
          }
        }

        LocalClass lc = new LocalClass();
        lc.test();
        break;
    }

    class LocalClass {
      public void test() {
        System.out.println("test1");
      }
    }

    LocalClass lc = new LocalClass();
    lc.test();
  }

  public void test1(int i) {
    switch (i) {
      case 0: {
        class LocalClass {
          public void test() {
            System.out.println("test");
          }
        }

        LocalClass lc = new LocalClass();
        lc.test();
        break;
      }
      case 1: {
        class LocalClass {
          public void test() {
            System.out.println("test1");
          }
        }

        LocalClass lc = new LocalClass();
        lc.test();
        break;
      }
    }

    class LocalClass {
      public void test() {
        System.out.println("test2");
      }
    }

    LocalClass lc = new LocalClass();
    lc.test();
  }

  public void test2(int i) {
    switch (i) {
      default:
        class LocalClass {
          public void test() {
            System.out.println("test");
          }
        }

        LocalClass lc = new LocalClass();
        lc.test();
        break;
    }

    class LocalClass {
      public void test() {
        System.out.println("test1");
      }
    }

    LocalClass lc = new LocalClass();
    lc.test();
  }
}
