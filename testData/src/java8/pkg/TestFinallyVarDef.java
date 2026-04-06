package pkg;

public class TestFinallyVarDef {
  public void test1() {
    int i;
    try {
      System.out.println("a");
    } finally {
      i = 5;
    }

    System.out.println(i);
  }

  public void test2() {
    int i = 2;
    try {
      System.out.println("a");
    } finally {
      i = 5;
    }

    System.out.println(i);
  }

  public void test3InCatch() {
    try {
      System.out.println("try");
    } catch (Exception ex) {
      System.out.println(ex);
      try {
        System.out.println(ex);
      } finally {
        System.out.println(ex);
      }
      System.out.println(ex);
    }
  }
}
