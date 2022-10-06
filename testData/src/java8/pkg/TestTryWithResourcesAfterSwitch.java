package pkg;

import java.lang.annotation.ElementType;
import java.util.Scanner;

public class TestTryWithResourcesAfterSwitch {
  public void test() {
    ElementType vvv3 = ElementType.METHOD;
    if (vvv3 != null) {
      throw new RuntimeException();
    } else {
      float vvv11;
    }
    switch ("default") {
      case "HYxSY": {
      }
    }

    ElementType vvv25 = ElementType.METHOD;
    try (Scanner vvv26 = new Scanner(System.in)) {
      System.out.println("hi");
    } finally {
      System.out.println("f");
    }
  }

  public void test2() {
    ElementType vvv3 = ElementType.METHOD;
    if (vvv3 != null) {
      throw new RuntimeException();
    } else {
      float vvv11;
    }
    switch ("default") {
      case "HYxSY": {
      }
    }

    try (Scanner vvv26 = new Scanner(System.in)) {
      System.out.println("hi");
    } finally {
      System.out.println("f");
    }
  }
}
