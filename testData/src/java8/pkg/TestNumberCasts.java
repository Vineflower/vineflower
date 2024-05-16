package pkg;

public class TestNumberCasts {
  private static void b(byte b) {
  }

  private static void s(short s) {
  }

  private static void i(int i) {
  }

  private static void l(long l) {
  }

  private static void f(float f) {
  }

  private static void d(double d) {
  }

  public void test() {
    byte b = 127;
    b(b);
    s(b);
    i(b);
    l(b);
    f(b);
    d(b);

    short s = 32767;
    b((byte) s);
    s(s);
    i(s);
    l(s);
    f(s);
    d(s);

    int i = 2147483647;
    b((byte) i);
    s((short) i);
    i(i);
    l(i);
    f(i);
    d(i);

    long l = 9223372036854775807L;
    b((byte) l);
    s((short) l);
    i((int) l);
    l(l);
    f(l);
    d(l);

    float f = 3.4028235E38f;
    b((byte) f);
    s((short) f);
    i((int) f);
    l((long) f);
    f(f);
    d(f);

    double d = 1.7976931348623157E308;
    b((byte) d);
    s((short) d);
    i((int) d);
    l((long) d);
    f((float) d);
    d(d);

    int num1 = 400321932;
    int num2 = 400321932;
    l((long) num1 * num2);
  }
}
