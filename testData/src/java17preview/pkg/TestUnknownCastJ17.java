package pkg;

import java.lang.annotation.ElementType;
import java.util.Scanner;

public class TestUnknownCastJ17 {
  public void test() {
    ElementType vvv1 = ElementType.METHOD;
    synchronized (this) {
      System.out.println(vvv1);
      switch (new Object()) {
        default:
          ElementType vvv2 = ElementType.METHOD;
      }
      try (Scanner vvv3 = new Scanner(System.in)) {
        String vvv4 = "Hi!";
      } finally {
        System.out.println(vvv1);
      }
      for (int vvv5 = 140; --vvv5 <= 395; vvv5 += -6) {
        for (int vvv6 = -91; ; vvv6 /= 33) {
          System.out.println(vvv1);
          break;
        }
      }
    }
    try {
      throw new RuntimeException();
    } catch (Exception vvv8) {
    }
  }

  public void test2() {
    ElementType vvv1 = ElementType.METHOD;
    synchronized (this) {
      System.out.println(vvv1);
      System.out.println(vvv1);
      switch (new Object()) {
        default:
          ElementType vvv2 = ElementType.METHOD;
      }
      System.out.println(vvv1);
      for (int vvv5 = 140; --vvv5 <= 395; vvv5 += -6) {
        for (int vvv6 = -91; ; vvv6 /= 33) {
          System.out.println(vvv1);
          break;
        }
      }
    }
    try {
      throw new RuntimeException();
    } catch (Exception vvv8) {
    }
  }
}
