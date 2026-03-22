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

  public void test1(int i, int j) {
    while (j > 10) {
      assert i > 0;
      if (i < j) {
        break;
      }

      i++;
    }
  }

  public void test2(int i, int j) {
    assert i > 0;

    while (j > 10) {
      if (i < j) {
        break;
      }

      j++;
    }
  }
}
