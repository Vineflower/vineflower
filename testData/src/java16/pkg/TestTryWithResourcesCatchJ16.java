package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesCatchJ16 {
    public void test(File file) {
        try (Scanner scanner = new Scanner(file)) {
            scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void testFunc(File file) {
        try (Scanner scanner = create(file)) {
            scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

  public int test1(File file) {
    int i = 0;

    try {
      System.out.println(-1);

      try (Scanner scanner = create(file); Scanner scanner2 = create(file)) {
        scanner.next();
        i++;
      }
    } catch (Exception e) {
      System.out.println(1);
    }

    if (i == 0) {
      System.out.println(0);
    } else {
      System.out.println(2);
    }

    return i;
  }

    private Scanner create(File file) throws FileNotFoundException {
        return new Scanner(file);
    }
}
