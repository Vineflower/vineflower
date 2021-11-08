package pkg;

import java.io.FileInputStream;
import java.io.InputStream;

public class TestTrySynchronized {
  private static TestTrySynchronized monitor = new TestTrySynchronized();

  public static void case01() throws Exception {

    synchronized(monitor) {
      System.out.println("Inside synchronized block.");
    }

    InputStream stream = null;
    try {
      stream = new FileInputStream("nul");
    } catch(Throwable e) {
      stream.close();
    }
  }

  public static void case02() throws Exception {

    synchronized(monitor) {
      System.out.println("Inside first synchronized block.");
    }

    InputStream stream = null;
    try {
      stream = getInputStream();

      synchronized(monitor) {
        System.out.println("Inside second synchronized block.");
      }

    } catch(Throwable e) {
      stream.close();
    }

  }

  private static InputStream getInputStream() {
    return null;
  }

}
