package pkg;

public class TestGenericMapping {
  class EntityA<Q extends EntityA<Q, R>, R extends EntityB<Q, R>> {
    public void doSomething(final Q q) {
    }
  }

  public class EntityB<S extends EntityA<S, T>, T extends EntityB<S, T>> {
    public void doSomething(final S t) {
      t.doSomething(t);
    }
  }
}
