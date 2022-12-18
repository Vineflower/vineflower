package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TestTryWithResourcesFakeTrigger {

  public void testTrigger1() {
    String a, b = "Hi!";

    try {
      try {
        System.out.println("Hi");
      } catch (Exception ignored) {
      }
      return;
    } catch (Exception ex) {
      try {
        a = b;
      } catch (Exception ignored) {
      }
    }
  }

  public void testTrigger2() {
    Object var1 = null;

    while (var1 == null) {
      try {
        System.out.println("Hi");
      } catch (Exception var21) {
        if (var1 != null) {
          break;
        }
        System.out.println(var1);
      }
    }
  }

  public void testTrigger3(File file) throws FileNotFoundException {
    Scanner scanner = new Scanner(file);

    try {
      scanner.next();
    } catch (NoSuchElementException e) {
      try {
        scanner.close();
      } catch (IllegalStateException e1) {
        e.addSuppressed(e1);
      }

      throw e;
    }
  }

  public void testTrigger4(File file) throws FileNotFoundException {
    Scanner scanner = new Scanner(file);

    try {
      scanner.next();
    } catch (Throwable e) {
      try {
        scanner.close();
      } catch (Throwable e1) {
        e.addSuppressed(e1);
      }

      throw e;
    }
  }
}
