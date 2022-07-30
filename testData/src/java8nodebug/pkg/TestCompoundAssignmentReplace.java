package pkg;

public class TestCompoundAssignmentReplace {
  private int i_1;
  private int i_2;
  private int i_3;
  private int i_4;
  private int i_5;

  public void test(int i, int j) {
    int k = i;

    i += j;

    int k1 = i;
    i += j;

    int k2 = i;
    if (j > 0) {
      i += j;
    }

    this.i_1 = i;
    this.i_2 = k;
    this.i_3 = k1;
    this.i_4 = k2;
  }
}
