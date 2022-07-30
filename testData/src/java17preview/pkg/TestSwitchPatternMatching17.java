package pkg;

public class TestSwitchPatternMatching17 {
  static void test4(Object o) {
    switch (o) {
      case String s && (
        switch(s.charAt(0)) {
          case 'a'-> true;
          case 'b'-> s.length() > 5;
          default-> false;
        }):
        System.out.println(s);
      default:
        throw new IllegalArgumentException("no");

    }
  }
}
