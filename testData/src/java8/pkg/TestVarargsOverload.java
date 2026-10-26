package pkg;

public class TestVarargsOverload {
  Object get(int index) {
    return get(new int[]{index});
  }

  Object get(int... indices) {
    return indices[0];
  }

  static String fmt(String pattern, Object... args) {
    return pattern + args.length;
  }

  static String fmt(String s, int start, String value) {
    return s + start + value;
  }

  static String insertLike(String s, int n, String v) {
    return fmt(s, new Object[]{n, v});
  }

  static String trim(String s, char... chars) {
    return s.trim();
  }

  static int trim(String s, char c) {
    return s.indexOf(c);
  }

  static String useTrim(String s) {
    return trim(s, new char[]{'"'});
  }
}
