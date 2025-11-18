package pkg;

import java.util.*;

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

  public <S> Collection<S> otherGeneric(List<? extends T> list) {
    return (Collection<S>) Collections.unmodifiableCollection(list);
  }

  public static <E> Other<E> makeNestedFromWildcard(Another<? extends TestGenericWildcard<?>> aa, TestGenericWildcard<?> bb) {
    return new Other<>((Another<? extends TestGenericWildcard<E>>)aa, (TestGenericWildcard<E>)bb);
  }

  public static class Other<C> {
    public Other(Another<? extends TestGenericWildcard<C>> a, TestGenericWildcard<C> b) {

    }
  }

  public static class Another<C> {

  }

  public <S> TestGenericWildcard<S> otherGenericUnmapped() {
    return new TestGenericWildcard<>();
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

  static class One<T> implements Comparable<One<?>> {
    public Comparator<T> cmp;
    public T obj;

    @Override
    public int compareTo(TestGenericWildcard.One<?> o) {
      return cmp.compare(obj, (T) o.obj);
    }
  }
}
