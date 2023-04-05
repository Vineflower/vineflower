package pkg;

public class TestTextTokens3 {
  public enum Entry {
    A,
    B,
    C,
    D;
  }

  public static String get(Entry e) {
    String s = "abc";
    switch (e) {
      case A:
        return "Hello world";
      case B:
        s = "Lorem ipsum";
      case C:
        s += " dolor sit amet";
      default:
        return s;
    }
  }
}
