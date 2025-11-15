package pkg;

import java.io.File;
import java.io.FileNotFoundException;

public class TestRecordPatterns8 {
  sealed interface Sealed {
    record R1(int x) implements Sealed {}

    record R2(String s) implements Sealed {}
  }

  public Object test1(File f) {
    try {
      Sealed i = get("aaaa");
      if (i instanceof Sealed.R1(int v)) {
        return v;
      } else if (i instanceof Sealed.R2(String w)) {
        return w;
      }
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  public Object test2(File f) {
    try {
      Sealed i = get("aaaa");
      if (i instanceof Sealed.R1(int v)) {
        System.out.println(v);
      } else if (i instanceof Sealed.R2(String w)) {
        return w;
      }
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  public Object test3(File f) {
    try {
      Sealed i = get("aaaa");
      if (i instanceof Sealed.R1(int v)) {
        return v;
      } else if (i instanceof Sealed.R2(String w)) {
        System.out.println(w);
      }
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  public Object test4(File f) {
    try {
      Sealed i = get("aaaa");
      if (i instanceof Sealed.R1(int v)) {
        return v;
      }
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  public Object test5(File f) {
    try {
      Sealed i = get("aaaa");
      if (i instanceof Sealed.R1(int v)) {
        return v;
      }
      System.out.println("end");
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  private Sealed get(String s) throws FileNotFoundException {
    return null;
  }
}
