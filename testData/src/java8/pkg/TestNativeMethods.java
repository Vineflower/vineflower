package pkg;

public class TestNativeMethods {
  public native void foo();

  public int bar(int i) {
    return i * i;
  }

  protected native void baz();

  public void bar1(String s) {
    System.out.println(s);
  }
}
