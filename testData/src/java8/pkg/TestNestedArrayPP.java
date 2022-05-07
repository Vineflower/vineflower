package pkg;

public class TestNestedArrayPP {
  public int[] a;
  public int[] b;
  public int[] c;
  public int[] d;
  public int[] e;
  public int[] f;

  public int i;
  public int v;
  public int v2;

  public void test() {
    a[b[c[d[e[f[i]]]]]] = v;
  }

  public void test1() {
    v = a[b[c[d[e[f[i]]]]]];
  }

  public void test2() {
    a[b[c[d[e[f[i++]++]++]++]++]++] = v;
  }

  public void test3() {
    v = a[b[c[d[e[f[i++]++]++]++]++]++]++;
  }
}
