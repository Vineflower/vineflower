package pkg;

public class TestRecordPatterns3 {
  record R() {

  }

  public void test1(R r) {
    if (r instanceof R()) {
      System.out.println(r);
    }
  }

  public void test2(R r) {
    if (r instanceof R) {
      System.out.println(r);
    }
  }

  public void test3(R r) {
    if (r instanceof R x) {
      System.out.println(x);
    }
  }

  public void test4(Object r) {
    if (r instanceof R()) {
      System.out.println(r);
    }
  }

  public void test5(Object r) {
    if (r instanceof R) {
      System.out.println(r);
    }
  }

  public void test6(Object r) {
    if (r instanceof R x) {
      System.out.println(x);
    }
  }
}
