package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryLoop2 {
  private boolean field;

  public void test(File file) {
    while (true) {
      try {
        if (this.field) {
          Scanner scanner = new Scanner(file);

          continue;
        }

        break;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
}
