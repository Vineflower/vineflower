public class TestJava1Synchronized {
  public void test1(int in) {
    synchronized (this) {
      if (in == 0) {
        System.out.println("0");
        return;
      }

      System.out.println("1");
    }

    System.out.println("2");
  }

  public void test2(int in) {
    synchronized (this) {
      for (int i = 0; i < in; i++) {
        System.out.println("hello");
      }
    }
  }

  public void test3() {
    try {
      synchronized (this) {
        System.out.println("hello");
      }
    } finally {
      System.out.println("finally");
    }
  }

  public void test4() {
    try {
      System.out.println("try");
    } finally {
      synchronized (this) {
        System.out.println("hello");
      }
    }
  }

}