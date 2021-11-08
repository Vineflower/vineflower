package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Scanner;
import java.util.ServiceConfigurationError;

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

  public boolean hasNext(Path p, Iterator<File> f) {
    File a;
    while (true) {
      try {
        if (Files.exists(p)) {
          a = f.next();
          return true;
        } else
          return false;
      } catch (ServiceConfigurationError e) {
        System.out.println(1);
      } catch (NoClassDefFoundError e) {
        System.out.println(2);
      }
    }
  }
}
