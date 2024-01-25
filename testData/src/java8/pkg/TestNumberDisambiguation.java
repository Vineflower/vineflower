package pkg;

public class TestNumberDisambiguation {
  public void foo(byte b) {
  }

  public void foo(short s) {
  }

  public void foo(char c) {
  }

  public void foo(int i) {
  }

  public void foo(long l) {
  }

  public void foo(float f) {
  }

  public void foo(double d) {
  }

  public void test() {
    int i = 24;
    foo((byte) i);
    foo((short) i);
    foo((char) i);
    foo(i);
    foo((long) i);
    foo((float) i);
    foo((double) i);
  }
}
