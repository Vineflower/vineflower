package pkg;

import java.io.InputStream;

public class TestSynchronizedTrySharing {
  public void test1(String name) throws Exception {
    synchronized (name) {
      System.out.println(name);
    }

    InputStream is = null;
    try{
      is = new java.io.FileInputStream(name);
      name = name.substring(50);
    } catch (Exception e) {
      is.close();
    }
  }

  public void test2(String name) throws Exception {
    synchronized (name) {
      System.out.println(name);
    }

    InputStream is = null;
    try{
      is = new java.io.FileInputStream(name);
      synchronized (name) {
        System.out.println(name);
        name = name.substring(50);
      }
    } catch (Exception e) {
      is.close();
    }
  }
}
