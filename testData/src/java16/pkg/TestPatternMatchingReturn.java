package pkg;

public class TestPatternMatchingReturn {
  public String getPattern(Object obj) {
    if (obj instanceof String s) {
      return s;
    }

    System.out.println("filler");

    return null;
  }

  public String get(Object obj) {
    if (obj instanceof String) {
      return (String) obj;
    }

    System.out.println("filler");

    return null;
  }
}
