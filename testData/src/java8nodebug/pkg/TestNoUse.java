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

  public void testPPI(boolean b, int i) {
    int j = this.a[0]++;
    int k = ++this.a[1];

    if (b) {
      k = ++this.a[2];
    }

    System.out.println(j);
  }

  public void testNeg(boolean b, int i) {
    int j = this.a[0]++;
    int k = this.a[1]--;

    if (b) {
      k = this.a[2]--;
    }

    System.out.println(j);
  }

  public void test1(boolean b, int i) {
    int j = this.a[0]++;
    int k = i++;

    if (b) {
      k = i++;
    }

    System.out.println(j);
  }

  public void testTiny(int i) {
    i++;
  }

  public void testUse(boolean b, int i) {
    int j = this.a[0]++;
    int k = this.a[1]++;

    if (b) {
      k = this.a[2]++;
    }

    System.out.println(k);
  }
}
