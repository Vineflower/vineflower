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

  public void testDouble(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.get()[j] = holder.get()[j] + inc;
    }
  }

  public void testIdx(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.get()[holder.idx()] += inc;
    }
  }

  public void testIdxDouble(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.get()[holder.idx()] = holder.get()[holder.idx()] + inc;
    }
  }
  public void test1Double(Holder holder, int i, double inc) {
    holder.get()[i] = holder.get()[i] + inc;
  }

  public void test1Idx(Holder holder, int i, double inc) {
    holder.get()[holder.idx()] += inc;
  }

  public void test1IdxDouble(Holder holder, int i, double inc) {
    holder.get()[holder.idx()] = holder.get()[holder.idx()] + inc;
  }
  public void test2Double(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.a[j] = holder.a[j] + inc;
    }
  }

  public void test2Idx(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.a[holder.idx()] += inc;
    }
  }

  public void test2IdxDouble(Holder holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder.a[holder.idx()] = holder.a[holder.idx()] + inc;
    }
  }

  public void test3Double(Holder holder, int i, double inc) {
    holder.a[i] = holder.a[i] + inc;
  }

  public void test3Idx(Holder holder, int i, double inc) {
    holder.a[holder.idx()] += inc;
  }

  public void test3IdxDouble(Holder holder, int i, double inc) {
    holder.a[holder.idx()] = holder.a[holder.idx()] + inc;
  }

  public class Holder {
    public double[] a;

    public double[] get() {
      double[] res = a;
      a = new double[(int) (Math.random() * 50)];
      return res;
    }

    public int idx() {
      a = new double[(int) (Math.random() * 50)];
      return (int) (Math.random() * 50);
    }
  }
}
