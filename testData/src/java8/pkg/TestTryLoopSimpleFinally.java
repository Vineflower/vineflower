package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TestTryLoopSimpleFinally {
  private boolean field;

  public void test(File file) {
    try {
      while (this.field) {
        Scanner scanner = new Scanner(file);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      System.out.println("Finally");
    }
  }

  public void test2(int x, Path file) throws IOException {
    try {
      while (x >= 0) {
        Scanner scanner = new Scanner(file);

        if (x % 11 == 0) {
          System.out.println("nice");
          return;
        }

        x -= scanner.nextInt();
      }
    } finally {
      System.out.println("Finally");
    }
  }
}
