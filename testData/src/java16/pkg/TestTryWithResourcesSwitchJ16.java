package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesSwitchJ16 {
  public void test(File f) throws FileNotFoundException {
    try (Scanner s = create(f)) {
      switch (s.nextInt()) {
        case 1:
          System.out.println("1");
          break;
        case 2:
          System.out.println("2");
          break;
        default:
          System.out.println("default");
      }
    }
  }

  public void test1(File f) throws FileNotFoundException {
    label:
    try (Scanner s = create(f)) {
      switch (s.nextInt()) {
        case 1:
          System.out.println("1");
          break;
        case 2:
          System.out.println("2");
          break label;
        default:
          System.out.println("default");
      }

      System.out.println("after switch");
    }

    System.out.println("after");
    if (f.exists()) {
      System.out.println("exists");
    }
  }

  public int test2(File f) throws FileNotFoundException {
    label:
    try (Scanner s = create(f)) {
      switch (s.nextInt()) {
        case 1:
          System.out.println("1");
          break;
        case 2:
          System.out.println("2");
          break label;
        case 3:
          System.out.println("3");
          return 1;
        default:
          System.out.println("default");
      }

      System.out.println("after switch");
    }

    System.out.println("after");
    if (f.exists()) {
      System.out.println("exists");
    }

    return 0;
  }

  private Scanner create(File file) throws FileNotFoundException {
    return new Scanner(file);
  }
}
