package pkg;

public class TestNoUse {
  private int[] a = new int[10];

  public void test(boolean b, int i) {
    int j = this.a[0]++;
    int k = this.a[1]++;

    if (b) {
      k = this.a[2]++;
    }

    System.out.println(j);
  }
}
