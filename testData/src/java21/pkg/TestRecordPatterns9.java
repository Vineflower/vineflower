package pkg;

import java.io.File;
import java.io.FileNotFoundException;

public class TestRecordPatterns9 {
  sealed interface Sealed {
    record R1(int x) implements Sealed {}

    record R2(String s) implements Sealed {}
  }

  public Object test1(File f) {
    Sealed i = get("aaaa");
    if (i instanceof Sealed.R1(int v)) {
      return v;
    } else if (i instanceof Sealed.R2(String w)) {
      return w;
    }

    return null;
  }

  private Sealed get(String s) {
    return null;
  }
}
