package pkg;

public class TestGenericWildcard<T> {
  public TestGenericWildcard<?> wildcard() {
    return null;
  }

  public TestGenericWildcard<T> generic() {
    return (TestGenericWildcard<T>) wildcard();
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
