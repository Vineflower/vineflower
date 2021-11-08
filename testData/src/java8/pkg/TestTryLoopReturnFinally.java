package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryLoopReturnFinally {
  private boolean field;

  public void test(File file) {
    try {
      while (this.field) {
        if (file == null) {
          return;
        }

        Scanner scanner = new Scanner(file);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      System.out.println("Finally");
    }
  }
}
