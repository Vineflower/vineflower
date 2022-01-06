package pkg;

public class TestTernaryReturn {
  public Object a;
  public Object b;

  public boolean test(Object o) {
    if (!(o instanceof TestTernaryReturn)) {
      return false;
    }

    TestTernaryReturn p = (TestTernaryReturn) o;

    return (a == null ? p.a == null : a.equals(p.a)) && (b == null ? p.b == null : b.equals(p.b));
  }
}
