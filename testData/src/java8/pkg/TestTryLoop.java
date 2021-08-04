package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryLoop {
  private boolean field;

  public void test(File file) {
    try {
      while (this.field) {
        Scanner scanner = new Scanner(file);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
