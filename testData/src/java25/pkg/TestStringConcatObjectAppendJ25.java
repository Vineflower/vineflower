package pkg;

import java.util.Vector;

public class TestStringConcatObjectAppendJ25 {
  public static void store(Vector values) {
    String text = "";
    for (int i = 0; i < values.size(); i++) {
      text = text + values.elementAt(i) + ";";
    }
    sink(text);
    sink(String.valueOf(text));
  }



  private static void sink(String value) {
  }
}