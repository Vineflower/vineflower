package pkg;

import org.vineflower.marker.UnCatchException;

import java.io.File;
import java.net.URL;
import java.util.List;

public class TestTryCatchNoIncrement {
  public final void baseline(List<String> items) {
    try {
      for (int i = 0; i < items.size(); i++) {
        String item = items.get(i);
        System.out.println(i + ": " + item);
      }
    }
    catch (Throwable ex2) {
      ex2.printStackTrace();
    }
  }

  public final void skippedInc(List<String> items) {
    try {
      int i = 0;
      while (i < items.size()) {
        String item = items.get(i);
        System.out.println(i + ": " + item);
        try {
          i++;
        } catch (UnCatchException ex) {
        }
      }
    }
    catch (Throwable ex2) {
      ex2.printStackTrace();
    }

  }
  public final void skipPrinting(List<String> items) {
    try {
      for (int i = 0; i < items.size(); i++) {
        String item = items.get(i);
        try {
          System.out.println(i + ": " + item);
        } catch (UnCatchException ex) {
        }
      }
    }
    catch (Throwable ex2) {
      ex2.printStackTrace();
    }
  }
}
