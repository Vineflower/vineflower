package pkg;

public class TestAssertMerge {
  public void test(int i, int j) {
    while (true) {
      assert i > 0;
      if (i < j) {
        break;
      }

      i++;
    }
  }
}
