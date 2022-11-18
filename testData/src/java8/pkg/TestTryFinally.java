package pkg;

public class TestTryFinally {
  public void test0() {
    try {
      System.out.println("Hello");
    } finally {
      long l = 5;
    }
  }

  public void test1() {
    try {
      System.out.println("Hello");
    } finally {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }


  public void test2(int i) {
    try {
      System.out.println("Hello");
    } finally {
      System.out.println("Finally");
      if(i > 0) {
        System.out.println(i);
        return;
      }
    }

    System.out.println("Bye");
  }
}
