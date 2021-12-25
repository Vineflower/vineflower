package pkg;

public class TestGenericsInvocUnchecked<T extends Number> {
  public class Inner {
    public void test(int i, TestGenericsInvocUnchecked<T> t1, TestGenericsInvocUnchecked<T> t2) {

    }
  }

  public void test(int i, TestGenericsInvocUnchecked<?> other) {
    new Inner().test( i, this, (TestGenericsInvocUnchecked<T>) other);
  }
}
