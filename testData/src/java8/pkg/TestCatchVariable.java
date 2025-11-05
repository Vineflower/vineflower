package pkg;

import java.io.IOException;
import java.util.Random;

public class TestCatchVariable {
  public void test1() {
    try {
      System.out.println("Hello world!");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void test2() {
    try {
      System.out.println("Hello world!");
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public void test3() {
    try {
      throw new IOException();
    } catch (IOException ioEx) {
      ioEx.printStackTrace();
    }
  }

  public void test4() {
    try {
      System.out.println("Hello world!");
    } catch (Exception ex) {
      if (new Random().nextBoolean()) {
        System.out.println("bool");
      }

      ex.printStackTrace();
    }
  }

  public void test5() {
    try {
      System.out.println("Hello world!");
    } catch (Exception ex) {
      System.out.println("no one's there...");
    }
  }

  public void test6() {
    System.out.println("Hello");

    boolean b;
    try {
      b = true;
    } catch (Exception ex) {
      b = false;
    }

    System.out.println(b);
  }

  public int test7() {
    try {
      System.out.println("Hello world!");
    } catch (Exception ex) {
      return 5;
    }

    return 4;
  }

  public int test8() {
    try {
      return 4;
    } catch (Exception ex) {
      return 5;
    }
  }

  // No LVT is introduced for this variable, so none is expected here
  public void test9() {
    try {
      System.out.println("Hello world!");
    } catch (Exception ex) {
    }
  }
}
