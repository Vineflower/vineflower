package pkg;

public class TestIntBoolMerge {
  public void test() {
    {
      int i = 0;
      System.out.println(i);
    }
    {
      boolean i = true;
      System.out.println(i);
    }
  }

  public int testLoop(String str, String sub) {
    int count = 0;

    for (int i = 0; (i = str.indexOf(sub, i)) != -1; i += sub.length()) {
      ++count;
    }

    return count;
  }

  public long field1;
  public long field2;
  public void testField() {
    m1("s", field1 = field2++);
  }

  public void m1(Object o, long l) {

  }

  public void m1(String s, long l) {

  }
}
