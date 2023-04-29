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
}
