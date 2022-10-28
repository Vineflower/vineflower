package pkg;

public class TestGenericsInvocUnchecked<T extends Number> {
  public class Inner {
    public void testInner(int i, TestGenericsInvocUnchecked<T> t1, TestGenericsInvocUnchecked<T> t2) {

    }
  }

  public void test(int i, TestGenericsInvocUnchecked<?> other) {
    new Inner().testInner( i, this, (TestGenericsInvocUnchecked<T>) other);
  }

  public void test1(Class<?> c, String s) {
    Enum.valueOf((Class<? extends Enum>) c, s);
  }
}
