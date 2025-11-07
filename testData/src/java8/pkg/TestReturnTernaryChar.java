package pkg;

public class TestReturnTernaryChar {
    public int testChar(String input) {
        return "hello".equals(input) ? 34114 : 35311;
    }

    public int testShortChar(String input) {
        return "hello".equals(input) ? 32744 : 32321;
    }

    public int testByteChar(String input) {
        return "hello".equals(input) ? 34 : 21;
    }

    public int get(int v) {
      int w = v == 0 ? 36009 : v;
      System.out.println(w);
      return w;
    }
}
