package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryLoopRecompile {
  private boolean field;

  public void test(File file) {
    while(true) {
      try {
        if (this.field) {
          new Scanner(file);
          continue;
        }
      } catch (FileNotFoundException var3) {
        var3.printStackTrace();
      }

      return;
    }
  }
}
