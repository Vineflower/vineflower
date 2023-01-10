package org.jetbrains.java.decompiler.collections;

import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory.FastSparseSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FastSparseSetTest {
  private static <T> void assertPlainSetIsEqual(FastSparseSet<T> set, Set<T> expected) {
    assertEquals(expected, set.toPlainSet());
  }

  private static <T> void iteratorVisitsEachOnce(FastSparseSet<T> set, Collection<T> expected) {
    Set<T> ref = new HashSet<>(expected);

    for (T t : set) {
      assertTrue(ref.remove(t));
    }
  }

  private static <T> void iteratorVisitsInOrder(FastSparseSet<T> set, List<T> expected) {
    Iterator<T> listIterator = expected.iterator();
    for (T element : set) {
      do {
        assertTrue(listIterator.hasNext());
      } while (listIterator.next() != element);
    }
  }

  private static <T> void exhaustedIteratorReturnsNull(FastSparseSet<T> set) {
    Iterator<T> iterator = set.iterator();
    while (iterator.hasNext()) {
      iterator.next();
    }
    assertNull(iterator.next());
  }

  private static <T> void copyElements(List<T> list, FastSparseSet<T> set) {
    for (T element : list) {
      set.add(element);
    }
  }


  // newly created sets should be empty
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void newEmptySetIsEmpty(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    FastSparseSet<T> set = factory.createEmptySet();

    assertTrue(set.isEmpty());
    assertTrue(set.toPlainSet().isEmpty());
    assertFalse(set.iterator().hasNext());
  }

  // an exhausted iterator of an newly created empty set returns null
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void exhaustedIteratorOfEmptySetReturnsNull(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    FastSparseSet<T> set = factory.createEmptySet();
    exhaustedIteratorReturnsNull(set);
  }

  // adding random elements should appropriately modify the set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingElements(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    FastSparseSet<T> set = factory.createEmptySet();

    copyElements(elements, set);

    assertEquals(elements.size(), set.toPlainSet().size());
    iteratorVisitsEachOnce(set, elements);
    exhaustedIteratorReturnsNull(set);
  }

  // adding random elements should appropriately modify the set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingRandomElements(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    FastSparseSet<T> set = factory.createEmptySet();
    Random random = newRandom();

    copyElements(elements, set);
    List<T> copy = new ArrayList<>(elements);

    for (int i = 0; i < 10; i++) {
      T element = elementCreator.apply(random);
      set.add(element);
      copy.add(element);
    }

    assertEquals(copy.size(), set.toPlainSet().size());
    iteratorVisitsEachOnce(set, copy);
    exhaustedIteratorReturnsNull(set);
  }

  // adding random elements should cause copies to be equal
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingRandomElementsCopyEquals(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    FastSparseSet<T> set = factory.createEmptySet();
    FastSparseSet<T> set2 = factory.createEmptySet();
    FastSparseSet<T> set3 = factory.createEmptySet();
    Random random = newRandom();

    copyElements(elements, set);
    copyElements(elements, set2);

    for (int i = 0; i < 10; i++) {
      T element = elementCreator.apply(random);
      set.add(element);

      if (random.nextBoolean()) {
        set2.add(element);
      }
    }

    assertEquals(set, set.getCopy());
    assertNotEquals(set, set2);
    assertNotEquals(set, set2.getCopy());
    assertNotEquals(set2, set);
    assertNotEquals(set, set3);
  }

  // ensures cardinality calculations are correct
  //
  // size == 0: cardinality = 0
  // size == 1: cardinality = 1
  // size >  1: cardinality = 2
  @ParameterizedTest
  @MethodSource("emptyFactories")
  <T> void cardinalityInvariants(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    FastSparseSet<T> set = factory.createEmptySet();
    Random random = newRandom();

    assertEquals(0, set.getCardinality());

    set.add(elementCreator.apply(random));
    assertEquals(1, set.getCardinality());

    set.add(elementCreator.apply(random));
    assertEquals(2, set.getCardinality());

    set.add(elementCreator.apply(random));
    assertEquals(2, set.getCardinality());

    set.add(elementCreator.apply(random));
    assertEquals(2, set.getCardinality());
  }

  // Items removed through the iterator are no longer in the set

  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void itemsRemovedThroughIteratorAreNoLongerInSet(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    FastSparseSet<T> set = factory.createEmptySet();
    Random random = newRandom();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      Iterator<T> iterator = set.iterator();
      while (iterator.hasNext()) {
        T next = iterator.next();
        if (random.nextInt(5 + i / 3) == 0) {
          iterator.remove();
          assertFalse(set.contains(next));
        }
      }
    }
  }

  // set doesn't contain elements not added to set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void setDoesntContainNonAddedElements(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    Random random = newRandom();
    FastSparseSet<T> set = factory.createEmptySet();

    for (int i = 0; i < 10; i++) {
      T element = elementCreator.apply(random);
      assertFalse(set.contains(element));
    }
  }

  // set shouldn't contain a different set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void fullSetDoesntContainDifferent(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    Random random = newRandom();
    FastSparseSet<T> set1 = factory.createEmptySet();
    FastSparseSet<T> set2 = factory.createEmptySet();

    copyElements(elements, set1);
    copyElements(elements, set2);

    for (T t : set2.toPlainSet()) {
      if (random.nextBoolean()) {
        set2.remove(t);
      }
    }

    // To prevent against false positives in the case of only 1 element
    if (set2.toPlainSet().size() != set1.toPlainSet().size()) {
      boolean accumulate = true;
      for (T t : set1.toPlainSet()) {
        accumulate &= set2.contains(t);
      }

      assertFalse(accumulate);
    }
  }

  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void fullSetContainsSubset(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    Random random = newRandom();
    FastSparseSet<T> set1 = factory.createEmptySet();
    FastSparseSet<T> set2 = factory.createEmptySet();

    copyElements(elements, set1);
    copyElements(elements, set2);

    for (T t : set2.toPlainSet()) {
      if (random.nextBoolean()) {
        set2.remove(t);
      }
    }

    // To prevent against false positives in the case of only 1 element
    if (set2.toPlainSet().size() != set1.toPlainSet().size()) {
      boolean accumulate = true;
      for (T t : set2.toPlainSet()) {
        accumulate &= set1.contains(t);
      }

      assertTrue(accumulate);
    }
  }

  // union() works when combining two halves of a whole
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void unionWorksHalves(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    Random random = newRandom();
    FastSparseSet<T> domain = factory.createEmptySet();
    copyElements(elements, domain);
    FastSparseSet<T> set1 = factory.createEmptySet();
    FastSparseSet<T> set2 = factory.createEmptySet();

    Set<T> ps = domain.toPlainSet();
    for (int i = 0; i < ps.size(); i++) {
      if (random.nextBoolean()) {
        set1.add(elements.get(i));
      } else {
        set2.add(elements.get(i));
      }
    }

    set1.union(set2);

    assertEquals(domain.toPlainSet().size(), set1.toPlainSet().size());

    assertPlainSetIsEqual(set1, domain.toPlainSet());
    iteratorVisitsInOrder(set1, elements);
  }

  // union() works when adding new elements
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void unionWorksNewElements(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    Random random = newRandom();
    FastSparseSet<T> domain = factory.createEmptySet();
    copyElements(elements, domain);
    FastSparseSet<T> set1 = factory.createEmptySet();

    List<T> copy = new ArrayList<>(elements);

    for (int i = 0; i < 20; i++) {
      T element = elementCreator.apply(random);
      set1.add(element);
      copy.add(element);
    }

    set1.union(domain);

    iteratorVisitsInOrder(set1, copy);

    assertEquals(copy.size(), set1.toPlainSet().size());

    for (T t : copy) {
      assertTrue(set1.contains(t));
    }

    // Add set1 to domain

    domain.union(set1);

    assertEquals(copy.size(), domain.toPlainSet().size());

    for (T t : copy) {
      assertTrue(domain.contains(t));
    }
  }

  // intersection() works
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void intersectionWorks(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    Random random = newRandom();
    FastSparseSet<T> domain = factory.createEmptySet();
    copyElements(elements, domain);
    FastSparseSet<T> set1 = factory.createEmptySet();

    Set<T> ps = domain.toPlainSet();
    for (int i = 0; i < ps.size(); i++) {
      if (random.nextBoolean()) {
        set1.add(elements.get(i));
      }
    }

    domain.intersection(set1);
    List<T> copy = new ArrayList<>(elements);
    copy.retainAll(set1.toPlainSet());

    assertEquals(domain.toPlainSet().size(), set1.toPlainSet().size());

    assertPlainSetIsEqual(set1, domain.toPlainSet());
    iteratorVisitsInOrder(set1, copy);
  }

  // complement() works

  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void complementWorks(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    Random random = newRandom();
    FastSparseSet<T> domain = factory.createEmptySet();
    copyElements(elements, domain);
    FastSparseSet<T> set1 = factory.createEmptySet();
    FastSparseSet<T> set2 = factory.createEmptySet();

    Set<T> ps = domain.toPlainSet();
    for (int i = 0; i < ps.size(); i++) {
      if (random.nextBoolean()) {
        set1.add(elements.get(i));
      } else {
        set2.add(elements.get(i));
      }
    }

    domain.complement(set1);
    List<T> copy = new ArrayList<>(elements);
    copy.retainAll(set2.toPlainSet());

    assertEquals(domain.toPlainSet().size(), set2.toPlainSet().size());

    assertPlainSetIsEqual(set2, domain.toPlainSet());
    iteratorVisitsInOrder(set2, copy);
  }

  // toString() should never be empty
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void toStringNotEmpty(List<T> elements, FastSparseSetFactory<T> factory, Function<Random, T> elementCreator) {
    FastSparseSet<T> set = factory.createEmptySet();
    copyElements(elements, set);
    assertFalse(set.toString().isEmpty());
  }


  @ParameterizedTest
  @MethodSource("operationsOnVerySparseSetsSource")
  void operationsOnVerySparseSets(int sparseFactor, int overlapFactor) {
    Random random = newRandom();
    List<Integer> domain = IntStream.range(0, 1_000_000).boxed().collect(Collectors.toList());
    FastSparseSetFactory<Integer> factory = new FastSparseSetFactory<>(domain);

    FastSparseSet<Integer> set1 = factory.createEmptySet();
    FastSparseSet<Integer> set2 = factory.createEmptySet();

    for (int i = 0; i < 1_000_000; i++) {
      if (random.nextInt(sparseFactor) == 0) {
        set1.add(i);

        if (overlapFactor != 0 && random.nextInt(overlapFactor) == 0) {
          set2.add(i);
        }
      } else if (random.nextInt(sparseFactor - 1) == 0) {
        set2.add(i);
      }
    }

    Set<Integer> ps1 = set1.toPlainSet();
    Set<Integer> ps2 = set2.toPlainSet();

    // union
    FastSparseSet<Integer> union = set1.getCopy();
    union.union(set2);
    Set<Integer> psUnion = new HashSet<>(ps1);
    psUnion.addAll(ps2);

    assertPlainSetIsEqual(union, psUnion);

    // intersection
    FastSparseSet<Integer> intersection = set1.getCopy();
    intersection.intersection(set2);
    Set<Integer> psIntersection = new HashSet<>(ps1);
    psIntersection.retainAll(ps2);

    assertPlainSetIsEqual(intersection, psIntersection);

    // complement
    FastSparseSet<Integer> complement = set1.getCopy();
    complement.complement(set2);
    Set<Integer> psComplement = new HashSet<>(ps1);
    psComplement.removeAll(ps2);

    assertPlainSetIsEqual(complement, psComplement);
  }

  static Stream<Arguments> operationsOnVerySparseSetsSource() {
    return Stream.of(
        Arguments.of(10, 0),
        Arguments.of(10, 10),
        Arguments.of(10, 100),
        Arguments.of(100, 0),
        Arguments.of(100, 10),
        Arguments.of(100, 100),
        Arguments.of(1000, 0),
        Arguments.of(1000, 10),
        Arguments.of(1000, 100),
        Arguments.of(10000, 0),
        Arguments.of(10000, 10),
        Arguments.of(10000, 100)
    );
  }

  private static Random newRandom() {
    return new Random(0x50_B1A5ED); // so biased
  }

  private static Stream<Arguments> emptyFactories() {
    return Stream.of(
      new Key<>(List.of(), FastSparseSetTest::randomInt),
      new Key<>(List.of(), FastSparseSetTest::randomString)
    ).map(key -> Arguments.of(key.list, new FastSparseSetFactory<>(key.list), key.elementCreator));
  }

  private static Stream<Arguments> nonEmptyFactories() {
    return Stream.of(
      new Key<>(List.of(0), FastSparseSetTest::randomInt),
      new Key<>(List.of(1, 2, 3), FastSparseSetTest::randomInt),
      new Key<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), FastSparseSetTest::randomInt),
      new Key<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), FastSparseSetTest::randomInt),
      new Key<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), FastSparseSetTest::randomInt),
      new Key<>(List.of(0x0000_0000, 0x0000_1000, 0x0000_2000, 0x0000_4000, 0x0000_8000,
        0x0001_0000, 0x0010_0000, 0x0100_0000,
        0x1000_0000, 0x2000_0000, 0x4000_0000, 0x8000_0000,
        0xF000_0000, 0xFFFF_0000, 0xFFFF_0001, 0xFFFF_FFFF), FastSparseSetTest::randomInt),
      new Key<>(IntStream.range(0, 1024).boxed().collect(Collectors.toList()), FastSparseSetTest::randomInt),
      new Key<>(IntStream.range(0, 1024).mapToObj("A"::repeat).collect(Collectors.toList()), FastSparseSetTest::randomString)
    ).map(key -> Arguments.of(key.list, new FastSparseSetFactory<>(key.list), key.elementCreator));
  }

  private static int randomInt(Random random) {
    return random.nextInt();
  }

  private static String randomString(Random random) {
    return Integer.toHexString(random.nextInt());
  }

  private static final class Key<T> {
    private final List<T> list;
    private final Function<Random, T> elementCreator;

    private Key(List<T> list, Function<Random, T> elementCreator) {
      this.list = list;
      this.elementCreator = elementCreator;
    }
  }
}
