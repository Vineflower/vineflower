package pkg;

import java.util.Optional;

public class TestGenericWildcard<T> {
  public TestGenericWildcard<?> wildcard() {
    return null;
  }

  public TestGenericWildcard<T> generic() {
    return (TestGenericWildcard<T>) wildcard();
  }

  public <E> TestGenericWildcard<E> cast(TestGenericWildcard<? extends E> e) {
    return (TestGenericWildcard<E>) e;
  }

  public <E> TestGenericWildcard<E> castOtherType(TestGenericWildcard<? extends TestGenericWildcard<E>> e) {
    return (TestGenericWildcard<E>) e;
  }

  public <E> Optional<TestGenericWildcard<E>> castOptional(boolean bl, TestGenericWildcard<? extends TestGenericWildcard<E>> e) {
    return bl ? Optional.of((TestGenericWildcard<E>) e) : Optional.empty();
  }

  public <E> TestGenericWildcard<? super E> cast2(TestGenericWildcard<? extends E> e) {
    return (TestGenericWildcard<E>) e;
  }

  public TestGenericWildcard<TestGenericWildcard> typed() {
    return (TestGenericWildcard<TestGenericWildcard>) wildcard();
  }

  class Two<A, B> {
    public Two<?, B> wildcard() {
      return null;
    }

    public Two<A, B> generic() {
      return (Two<A, B>) wildcard();
    }

    public Two<Two, Two> typed() {
      return (Two<Two, Two>) wildcard();
    }
  }
}
