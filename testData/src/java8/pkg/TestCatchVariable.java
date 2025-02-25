package pkg;

import java.io.IOException;

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
}
