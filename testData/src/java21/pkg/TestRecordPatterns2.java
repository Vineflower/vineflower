package pkg;

public class TestRecordPatterns2 {
  record R(int x) {

  }

  public void test1(R r) {
    if (r instanceof R(int z)) {
      System.out.println(z);
    }
  }
}
