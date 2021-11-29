package pkg;

public class TestArrayAssign {
  public void test(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.get()[j] += inc;
    }
  }

  public void test1(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.a[j] += inc;
    }
  }

  public class Holder {
    public double[] a;

    public double[] get() {
      return a;
    }
  }
}
