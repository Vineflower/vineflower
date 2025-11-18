package pkg;

public class TestRecordPatterns1 {
  record R(int i, Object o) {}

  public void test1(R r) {
    if (r instanceof R(int x, Object o)) {
      System.out.println(x);
      System.out.println(o);
    }
  }

  public void test2(R r) {
    if (r instanceof R(int x, String s)) {
      System.out.println(x);
      System.out.println(s);
    }
  }

  public void test3(R r) {
    if (r instanceof R(int x, String s) && s.length() > 10) {
      System.out.println(x);
      System.out.println(s);
    }
  }

  public void test4(R r) {
    if (r instanceof R(int x, var v)) {
      System.out.println(x);
      System.out.println(v);
    }
  }
}
