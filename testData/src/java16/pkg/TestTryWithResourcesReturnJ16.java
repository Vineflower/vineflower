package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class TestTryWithResourcesReturnJ16 {
  public Scanner test(File file) throws FileNotFoundException {
    try (Scanner scanner = new Scanner(file)) {
      return scanner;
    }
  }

  public Scanner test2(File file) throws FileNotFoundException {
    try (Scanner scanner = new Scanner(file)) {
      System.out.println("try");
      return scanner;
    } catch (IOException e) {
      System.out.println("catch");
      throw new RuntimeException(e);
    }
  }

  public Scanner test3(File file) throws FileNotFoundException {
    try (Scanner scanner = new Scanner(file)) {
      return scanner;
    } catch (IOException e) {
      System.out.println("catch");
      return null;
    }
  }

  public Scanner test4(File file) throws FileNotFoundException {
    System.out.println("pre");

    try (Scanner scanner = new Scanner(file)) {
      return scanner;
    } catch (IOException e) {
      System.out.println("catch");
      return null;
    }
  }

  public Scanner test5(File file) throws FileNotFoundException {
    System.out.println("pre");

    try (Scanner scanner = new Scanner(file)) {
      System.out.println("try");
      return scanner;
    } catch (IOException e) {
      System.out.println("catch");
      return null;
    }
  }

  public Scanner test6(File file) throws FileNotFoundException {
    try (Scanner scanner = create(file)) {
      return scanner != null ? new Scanner(System.in) : scanner;
    } catch (IOException e) {
      System.out.println("catch");
      return null;
    }
  }

  public Scanner testFunc(File file) throws FileNotFoundException {
    try (Scanner scanner = create(file)) {
      return scanner;
    }
  }

  public Scanner testFinally(File file) {
    try (Scanner scanner = new Scanner(file)) {
      return scanner;
    } finally {
      return null;
    }
  }

  public Scanner testFinallyNested(File file) {
    try (Scanner scanner = new Scanner(file)) {
      try (Scanner scanner2 = new Scanner(file)) {
        return scanner2;
      } finally {
        return scanner;
      }
    } finally {
      return null;
    }
  }

  public String testComplex(File f, File f2, File f3) throws FileNotFoundException {
    String o;
    try (Scanner scanner = create(f);
         Scanner s2 = create(f2)) {
      scanner.next();

      if (!(scanner.hasNext() && s2.hasNext())) {
        return null;
      }

      try (Scanner s = create(f3)) {
        scanner.next();

        for (int i = 0; i < s.nextInt(); i++) {
          System.out.println(i);
        }

        o = s.next();
      }
    }

    return o;
  }

  public String testComplex1(File f, File f2, File f3) throws FileNotFoundException {
    try (Scanner scanner = create(f);
         Scanner s2 = create(f2)) {
      if ((scanner.hasNext() && s2.hasNext())) {
        return scanner.next();
      }

      s2.next();
    }

    return null;
  }

  public String testComplex2(File f, File f2, File f3) throws FileNotFoundException {
    try (Scanner scanner = create(f)) {
      if ((scanner.hasNext())) {
        return scanner.next();
      }

      scanner.next();
    }

    return null;
  }

  private Scanner create(File file) throws FileNotFoundException {
    return new Scanner(file);
  }
}
