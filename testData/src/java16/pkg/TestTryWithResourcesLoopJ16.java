package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesLoopJ16 {
  public void test(File f) throws FileNotFoundException {
    while (true) {
      try (Scanner s = create(f)) {
        if (!s.hasNext()) {
          break;
        }

        s.next();
      }
    }
  }

  public void test1(File f) throws FileNotFoundException {
    while (f.exists()) {
      try (Scanner s = create(f)) {
        if (!s.hasNext()) {
          break;
        }

        s.next();
      }
    }
  }

  public void test2(File f) throws FileNotFoundException {
    while (f.exists()) {
      try (Scanner s = create(f);
           Scanner s2 = create(f)) {
        if (!s.hasNext()) {
          break;
        }

        s.next();
      }
    }
  }

  public void test3(File f) throws FileNotFoundException {
    while (f.exists()) {
      try (Scanner s = create(f);
           Scanner s2 = create(f)) {
        if (!s.hasNext()) {
          break;
        } else if (s2.hasNext()) {
          return;
        }

        s.next();
      }
    }
  }

  public void test4(File f) throws FileNotFoundException {
    while (f.exists()) {
      try (Scanner s = create(f);
           Scanner s2 = create(f)) {
        if (!s.hasNext()) {
          continue;
        }

        s.next();
      }
    }
  }

  private Scanner create(File file) throws FileNotFoundException {
    return new Scanner(file);
  }
}
