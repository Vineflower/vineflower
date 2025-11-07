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

    public void sink1(char c) {

    }

    public void sink2(byte b) {

    }

    public int get(int v) {
      int w = v == 0 ? 36009 : v;
      System.out.println(w);
      return w;
    }

    public void get2(boolean b) {
      int v = b ? 0 : 1;
      get(v);
    }

    public void get3(boolean b) {
      sink1(b ? 'a' : 'A');
    }

    public void get4(boolean b) {
      sink2((byte) (b ? 0 : 1));
    }
}
