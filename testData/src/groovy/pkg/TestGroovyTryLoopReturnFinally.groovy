package pkg

public class TestGroovyTryLoopReturnFinally {
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
