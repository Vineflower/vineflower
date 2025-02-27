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

  public void bar(byte a, byte b) {
  }

  public void bar(short a, short b) {
  }

  public void bar(char a, char b) {
  }

  public void bar(int a, int b) {
  }

  public void bar(long a, long b) {
  }

  public void bar(float a, float b) {
  }

  public void bar(double a, double b) {
  }

  public void baz(int a, byte b, byte c) {
  }

  public void baz(int a, short b, short c) {
  }

  public void baz(int a, char b, char c) {
  }

  public void baz(int a, int b, int c) {
  }

  public void baz(int a, long b, long c) {
  }

  public void baz(int a, float b, float c) {
  }

  public void baz(int a, double b, double c) {
  }

  public void varargs(int... a) {
  }

  public void varargs(float... a) {
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

    bar((byte) 0, (byte) i);
    bar((short) 0, (short) i);
    bar('\u0000', (char) i);
    bar(0, i);
    bar(0L, i);
    bar(0.0F, i);
    bar(0.0, i);

    baz(0, (byte) 127, (byte) i);
    baz(0, (short) 32767, (short) i);
    baz(0, '\uFFFF', (char) i);
    baz(0, Integer.MAX_VALUE, i);
    baz(0, Long.MAX_VALUE, i);
    baz(0, Float.MAX_VALUE, i);
    baz(0, Double.MAX_VALUE, i);

    varargs(i);
    varargs((float) i);
  }
}
