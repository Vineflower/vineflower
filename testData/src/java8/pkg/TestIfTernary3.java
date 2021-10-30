package pkg;

public class TestIfTernary3 {
  public void test(boolean condition, int a, int b) {
    if (condition ? ("" + a) == "1" : ("" + b) == "4") {
      System.out.println(1);
    }

    System.out.println(2);
  }
}
