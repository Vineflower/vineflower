package pkg;

import java.util.Random;

public class TestComplexIfElseChain {
  public void test() {
    Random randy = new Random();
    int result = randy.nextInt(11);
    if (result == 0 || result == 1) {
      System.out.println("a");
    } else if (result == 2 || result == 3) {
      System.out.println("b");
    } else if (result == 4 || result == 5) {
      System.out.println("c");
    } else if (result == 6 || result == 7) {
      System.out.println("d");
    } else if (result == 8 || result == 9) {
      System.out.println("e");
    }
  }

  public void testInLoop() {
    Random randy = new Random();
    int result = randy.nextInt(11);
    for (int i = 0; i < 10; i++) {
      if (result == 0 || result == 1) {
        System.out.println("a");
      } else if (result == 2 || result == 3) {
        System.out.println("b");
      } else if (result == 4 || result == 5) {
        System.out.println("c");
      } else if (result == 6 || result == 7) {
        System.out.println("d");
      } else if (result == 8 || result == 9) {
        System.out.println("e");
      }
    }
  }
}
