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
}
