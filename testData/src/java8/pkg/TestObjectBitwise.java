package pkg;

public abstract class TestObjectBitwise<T> {
  abstract T get();

  public boolean test(int i) {
    return (((Long)obj()) & i) == 0;
  }

  public boolean testn(int i) {
    return (((Long)num()) & i) == 0;
  }

  public boolean testg(int i) {
    return (((Long)get()) & i) == 0;
  }

  public static class Inner extends TestObjectBitwise<Long> {
    private TestObjectBitwise<Long> other;

    @Override
    Long get() {
      return 10000L;
    }

    public boolean testg_inner(int i) {
      return ((get()) & i) == 0;
    }

    public boolean testg_inner2(int i) {
      long l = get();
      return (l & i) == 0;
    }

    public boolean testg_inner3(int i) {
      long l = other.get();
      return (l & i) == 0;
    }

    public boolean testg_inner4(int i) {
      long l = (Long)other.obj();
      long l2 = (Long)other.num();
      long l3 = (Long)obj();
      long l4 = (Long)num();
      return (l & i & l2 & l3 & l4) == 0;
    }
  }

  public boolean test1(int i) {
    return (((Long)obj()) | i) == 0;
  }

  public boolean test3(int i) {
    return (((Long)obj()) + i) == 0;
  }

  public boolean test4(int i) {
    return (((Long)obj()) % i) == 0;
  }

  public Object obj() {
    return 100000L;
  }

  public Number num() {
    return 100000L;
  }
}
