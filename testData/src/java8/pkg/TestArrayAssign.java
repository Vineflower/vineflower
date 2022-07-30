package pkg;

public class TestArrayAssign {
  public void test(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.get()[j] += inc;
    }
  }

  public void test1(Holder holder, int i, double inc) {
    holder.get()[i] += inc;
  }

  public void test2(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.a[j] += inc;
    }
  }

  public double test3(Holder holder, int i) {
    return holder.get()[i];
  }

  public void test4(Holder holder, int i, double inc) {
    inc += holder.get()[i];
    System.out.println(inc);
  }

  public class Holder {
    public double[] a;

    public double[] get() {
      return a;
    }
  }
}
