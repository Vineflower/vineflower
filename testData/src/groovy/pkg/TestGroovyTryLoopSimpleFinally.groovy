package pkg


import java.nio.file.Path

public class TestGroovyTryLoopSimpleFinally {
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
