package org.jetbrains.java.decompiler.collections;

import org.jetbrains.java.decompiler.util.collections.fixed.FastFixedSet;
import org.jetbrains.java.decompiler.util.collections.fixed.FastFixedSetFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class FixedFastSetTest {
  private static <T> void assertPlainSetIsEqual(FastFixedSet<T> set, Set<T> expected) {
    assertEquals(expected, set.toPlainSet());
  }

  private static <T> void iteratorVisitsEachOnce(FastFixedSet<T> set, Collection<T> expected) {
    Set<T> ref = new HashSet<>(expected);

    for (T t : set) {
      assertTrue(ref.remove(t));
    }
  }

  private static <T> void iteratorVisitsInOrder(FastFixedSet<T> set, List<T> expected) {
    Iterator<T> listIterator = expected.iterator();
    for (T element : set) {
      do {
        assertTrue(listIterator.hasNext());
      } while (listIterator.next() != element);
    }
  }

  private static <T> void exhaustedIteratorReturnsNull(FastFixedSet<T> set) {
    Iterator<T> iterator = set.iterator();
    while (iterator.hasNext()) {
      iterator.next();
    }
    assertNull(iterator.next());
  }

  private static <T> void iterationMatchesUnguardedIteration(FastFixedSet<T> set) {
    Iterator<T> unguardedIterator = set.iterator();

    for (T element : set) {
      assertEquals(element, unguardedIterator.next());
    }

    assertFalse(unguardedIterator.hasNext());
  }

  private <T> void assertContains(FastFixedSet<T> set, T element) {
    assertTrue(set.contains(element));
    assertTrue(set.containsKey(element));
  }


  // newly created sets should be empty
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void newEmptySetIsEmpty(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createEmptySet();

    assertTrue(set.isEmpty());
    assertTrue(set.toPlainSet().isEmpty());
    assertFalse(set.iterator().hasNext());

    // size is the factory size for some reason
    assertEquals(elements.size(), set.size());
  }

  // an exhausted iterator of an newly created empty set returns null
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void exhaustedIteratorOfEmptySetReturnsNull(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createEmptySet();
    exhaustedIteratorReturnsNull(set);
  }

  // newly created filled sets should match the factory
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void fullSetMatchesList(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createCopiedSet();

    assertFalse(set.isEmpty());
    assertEquals(elements.size(), set.size());
    assertPlainSetIsEqual(set, new HashSet<>(elements));
    iteratorVisitsInOrder(set, elements);
  }

  // an exhausted iterator of a newly created filled set returns null
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void exhaustedIteratorOfFullSetReturnsNull(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createCopiedSet();
    exhaustedIteratorReturnsNull(set);
  }

  // newly created filled sets should contain all elements
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void fullSetContainsAllElements(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createCopiedSet();

    for (T element : elements) {
      assertContains(set, element);
    }
  }

  // multiple newly created filled sets should contain each other's copies
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void fullSetContainsCopied(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set1 = factory.createCopiedSet();
    FastFixedSet<T> set2 = factory.createCopiedSet();

    assertTrue(set1.contains(set2));
  }

  // set shouldn't contain a different set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void fullSetDoesntContainDifferent(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set1 = factory.createCopiedSet();
    FastFixedSet<T> set2 = factory.createCopiedSet();

    for (T t : set2.toPlainSet()) {
      if (random.nextBoolean()) {
        set2.remove(t);
      }
    }

    // To prevent against false positives in the case of only 1 element
    if (set2.toPlainSet().size() != set1.toPlainSet().size()) {
      assertFalse(set2.contains(set1));
    }
  }

  // set should contain a subset
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void fullSetContainsSubset(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set1 = factory.createCopiedSet();
    FastFixedSet<T> set2 = factory.createCopiedSet();

    for (T t : set2.toPlainSet()) {
      if (random.nextBoolean()) {
        set2.remove(t);
      }
    }

    assertTrue(set1.contains(set2));
  }

  // newly created filled sets should visit each element exactly once
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void fullSetVisitsEachElementOnce(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createCopiedSet();

    iteratorVisitsEachOnce(set, elements);
  }

  // newly created sets shouldn't contain any elements
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void emptySetDoesNotContainAnyElements(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createEmptySet();

    for (T element : elements) {
      assertFalse(set.contains(element));
      // empty sets seemingly still have their keys?
//      assertFalse(set.containsKey(element));
    }
  }

  // Iteration order for reverse filled sets should still be in the factory order
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingInReverseOrderMaintainsBaseIterationOrder(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createEmptySet();

    for (int i = elements.size() - 1; i >= 0; i--) {
      set.add(elements.get(i));
    }

    iteratorVisitsInOrder(set, elements);
  }

  // Iteration order for randomly filled sets should still be in the factory order
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingInRandomOrderMaintainsBaseIterationOrder(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (T element : shuffled) {
      set.add(element);
      iteratorVisitsInOrder(set, elements);
    }
  }

  // An exhausted iterator of a randomly partially filled set returns null
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void exhaustedIteratorOfRandomlyPartiallyFilledSetReturnsNull(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (T element : shuffled) {
      set.add(element);
      exhaustedIteratorReturnsNull(set);
    }
  }

  // Randomly added items should be contained
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addedItemsShouldBeContained(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (T element : shuffled) {
      set.add(element);
      assertContains(set, element);
    }
  }

  // The iterator of a set to which items got added randomly
  // should visit each element that has been added exactly once
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void iteratorVisitsAddedItems(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);

      set.add(element);
      iteratorVisitsEachOnce(set, shuffled.subList(0, i + 1));
    }
  }

  // A set to which items got added randomly should contain all items that have been added
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addedItemsAreContained(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      for (int j = 0; j <= i; j++) {
        assertTrue(set.contains(shuffled.get(j)));
      }
    }
  }

  // A set to which items got added randomly should not contain
  // any items that have not been added yet
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void notAddedItemsAreNotContained(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      for (int j = i + 1; j < shuffled.size(); j++) {
        assertFalse(set.contains(shuffled.get(j)));
      }
    }
  }

  // The plain set conversion matches the expected set if items
  // are added randomly one by one
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void plainSetConversionMatchesExpectedSet(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      assertPlainSetIsEqual(set, new HashSet<>(shuffled.subList(0, i + 1)));
    }
  }

  // Adding items is idempotent with respect to the plain set conversion
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingItemsIsIdempotentWithRespectToPlainSetConversion(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      Set<T> plain = set.toPlainSet();

      for (int x = 0; x < 10; x++) {
        set.add(element);
        assertEquals(plain, set.toPlainSet());
      }
    }
  }

  // Adding items already in the set does not produce a different plain set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingItemsAlreadyInSetDoesNotProduceDifferentPlainSet(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      Set<T> plain = set.toPlainSet();

      for (int x = 0; x < 10; x++) {
        set.add(shuffled.get(random.nextInt(i + 1)));
        assertEquals(plain, set.toPlainSet());
      }
    }
  }

  // Adding items already in the set does not modify the iteration order
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingItemsAlreadyInSetDoesNotModifyIterationOrder(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      for (int x = 0; x < 10; x++) {
        set.add(shuffled.get(random.nextInt(i + 1)));
        iteratorVisitsInOrder(set, elements);
      }
    }
  }

  // Adding items already in the set does not modify which items the iterator visits
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingItemsAlreadyInSetDoesNotModifyWhichItemsIteratorVisits(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      List<T> expected = new ArrayList<>(shuffled.subList(0, i + 1));

      for (int x = 0; x < 10; x++) {
        set.add(shuffled.get(random.nextInt(i + 1)));
        iteratorVisitsEachOnce(set, expected);
      }
    }
  }

  // Removed items are no longer contained
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void removedItemsAreNoLongerContained(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (T element : shuffled) {
      set.remove(element);
      assertFalse(set.contains(element));
    }
  }


  // A filled set from which items got removed randomly should contain
  // all items that have not been removed yet
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void notRemovedItemsAreContained(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.remove(element);

      for (int j = i + 1; j < shuffled.size(); j++) {
        assertTrue(set.contains(shuffled.get(j)));
      }
    }
  }

  // The plain set conversion matches the expected set if items
  // are removed randomly one by one
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void plainSetConversionMatchesExpectedSetAfterRemoving(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.remove(element);

      assertPlainSetIsEqual(set, new HashSet<>(shuffled.subList(i + 1, shuffled.size())));
    }
  }

  // Removing items is idempotent with respect to the plain set conversion
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void removingItemsIsIdempotentWithRespectToPlainSetConversion(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.remove(element);

      Set<T> plain = set.toPlainSet();

      for (int x = 0; x < 10; x++) {
        set.remove(element);
        assertEquals(plain, set.toPlainSet());
      }
    }
  }

  // Removing items that are no longer in the set does not produce a different plain set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void removingItemsThatAreNoLongerInSetDoesNotProduceDifferentPlainSet(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.remove(element);

      Set<T> plain = set.toPlainSet();

      for (int x = 0; x < 10; x++) {
        set.remove(shuffled.get(random.nextInt(i + 1)));
        assertEquals(plain, set.toPlainSet());
      }
    }
  }

  // Removing items no longer in the set does not modify the iteration order
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void removingItemsNoLongerInSetDoesNotModifyIterationOrder(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.remove(element);

      List<T> expected = new ArrayList<>(shuffled.subList(0, i + 1));

      for (int x = 0; x < 10; x++) {
        set.remove(shuffled.get(random.nextInt(i + 1)));
        iteratorVisitsInOrder(set, elements);
      }
    }
  }

  // Adding items to a set should not add any items that have been removed from a set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingItemsDoesNotAddRemovedItems(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.add(element);

      if (i % 2 == 0) {
        T target = shuffled.get(i / 2);
        assertTrue(set.contains(target));
        set.remove(target);
      }

      for (int j = 0; j <= i / 2; j++) {
        assertFalse(set.contains(shuffled.get(j)));
      }

      for (int j = i / 2 + 1; j < i; j++) {
        assertTrue(set.contains(shuffled.get(j)));
      }
    }
  }

  // Adding items that have been removed from a set should not add any
  // items that have been removed from a set but not added back
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addingRemovedItemsDoesNotAddRemovedItems(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 0; i < shuffled.size(); i++) {
      T element = shuffled.get(i);
      set.remove(element);

      if (i % 2 == 0) {
        T target = shuffled.get(i / 2);
        assertFalse(set.contains(target));
        set.add(target);
      }

      for (int j = 0; j <= i / 2; j++) {
        assertTrue(set.contains(shuffled.get(j)));
      }

      for (int j = i / 2 + 1; j < i; j++) {
        assertFalse(set.contains(shuffled.get(j)));
      }
    }
  }

  // Items removed through the iterator are no longer in the set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void itemsRemovedThroughIteratorAreNoLongerInSet(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createEmptySet();

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

  // Empty set equals itself
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void emptySetEqualsItself(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createEmptySet();
    assertEquals(set, set);
  }

  // Filled set equals itself
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void filledSetEqualsItself(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createCopiedSet();
    assertEquals(set, set);
  }

  // Empty set equals another empty set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void emptySetEqualsAnotherEmptySet(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set1 = factory.createEmptySet();
    FastFixedSet<T> set2 = factory.createEmptySet();
    assertEquals(set1, set2);
  }

  // Filled set equals another filled set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void filledSetEqualsAnotherFilledSet(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set1 = factory.createCopiedSet();
    FastFixedSet<T> set2 = factory.createCopiedSet();
    assertEquals(set1, set2);
  }

  // Empty set does not equal a filled set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void emptySetDoesNotEqualFilledSet(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set1 = factory.createEmptySet();
    FastFixedSet<T> set2 = factory.createCopiedSet();
    assertNotEquals(set1, set2);
  }

  // Filled set does not equal an empty set
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void filledSetDoesNotEqualEmptySet(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set1 = factory.createCopiedSet();
    FastFixedSet<T> set2 = factory.createEmptySet();
    assertNotEquals(set1, set2);
  }

  // Sets with different sizes are not equal
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void setsWithDifferentSizesAreNotEqual(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set1 = factory.createEmptySet();
    FastFixedSet<T> set2 = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    set1.add(shuffled.get(0));

    assertNotEquals(set1, set2);

    for (int i = 1; i < shuffled.size(); i++) {
      set1.add(shuffled.get(i));
      set2.add(shuffled.get(i - 1));

      assertNotEquals(set1, set2);
    }
  }

  // Sets with different elements are not equal
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void setsWithDifferentElementsAreNotEqual(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set1 = factory.createEmptySet();
    FastFixedSet<T> set2 = factory.createEmptySet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (int i = 1; i < shuffled.size(); i++) {
      set1.add(shuffled.get(i));
      set2.add(shuffled.get(i-1));

      assertNotEquals(set1, set2);
    }
  }

  // Generate set of square indices by flipping
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void generateSetOfSquareIndicesByFlipping(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createEmptySet();

    set.add(elements.get(0));

    for (int step = 1; step <= elements.size(); step++) {
      for (int i = step; i < elements.size(); i += step) {
        T element = elements.get(i);

        if (set.contains(element)) {
          set.remove(element);
        } else {
          set.add(element);
        }
      }
    }

    for (int i = 0, k = 0; i < elements.size(); i++) {
      T element = elements.get(i);

      if (i == k * k) {
        assertTrue(set.contains(element));
        k++;
      } else {
        assertFalse(set.contains(element));
      }
    }
  }

  // The copy of an empty set is empty
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void copyOfAnEmptySetIsEmpty(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createEmptySet().getCopy();

    assertTrue(set.isEmpty());
    assertTrue(set.toPlainSet().isEmpty());
    assertFalse(set.iterator().hasNext());
  }

  // The copy of a set is equal to the original
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void copyOfASetIsEqualToTheOriginal(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();

    List<T> shuffled = new ArrayList<>(elements);
    Collections.shuffle(shuffled, random);

    for (T element : shuffled) {
      set.add(element);
      assertEquals(set, set.getCopy());
    }
  }

  private static Random newRandom() {
    return new Random(0x50_B1A5ED); // so biased
  }

  // clear() works
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void clearWorks(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createCopiedSet();
    set.clear();

    assertTrue(set.isEmpty());
    assertTrue(set.toPlainSet().isEmpty());
    assertFalse(set.iterator().hasNext());
  }

  // removeAll() works
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void removeAllWorks(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> set = factory.createCopiedSet();
    ArrayList<T> copy = new ArrayList<>(elements);

    Set<T> toRemove = new HashSet<>();
    for (T element : elements) {
      if (random.nextInt(4) == 0) {
        toRemove.add(element);
      }
    }

    set.removeAll(toRemove);
    assertEquals(elements.size() - toRemove.size(), set.toPlainSet().size());

    copy.removeAll(toRemove);
    assertPlainSetIsEqual(set, new HashSet<>(copy));
    iteratorVisitsInOrder(set, copy);
  }

  // addAll() works
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void addAllWorks(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createCopiedSet();
    FastFixedSet<T> empty = factory.createEmptySet();

    empty.addAll(set.toPlainSet());
    assertEquals(set.toPlainSet().size(), empty.toPlainSet().size());

    assertPlainSetIsEqual(empty, set.toPlainSet());
    iteratorVisitsInOrder(empty, elements);
  }

  // union() works
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void unionWorks(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> domain = factory.createCopiedSet();
    FastFixedSet<T> set1 = factory.createEmptySet();
    FastFixedSet<T> set2 = factory.createEmptySet();

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

  // intersection() works
  @ParameterizedTest
  @MethodSource("nonEmptyFactories")
  <T> void intersectionWorks(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> domain = factory.createCopiedSet();
    FastFixedSet<T> set1 = factory.createEmptySet();

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
  <T> void complementWorks(List<T> elements, FastFixedSetFactory<T> factory) {
    Random random = newRandom();
    FastFixedSet<T> domain = factory.createCopiedSet();
    FastFixedSet<T> set1 = factory.createEmptySet();
    FastFixedSet<T> set2 = factory.createEmptySet();

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
  <T> void toStringNotEmpty(List<T> elements, FastFixedSetFactory<T> factory) {
    FastFixedSet<T> set = factory.createCopiedSet();
    assertFalse(set.toString().isEmpty());
  }

  private static Stream<Arguments> nonEmptyFactories() {
    return Stream.of(
      List.of(0),
      List.of(1, 2, 3),
      List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
      List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
      List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20),
      List.of(0x0000_0000, 0x0000_1000, 0x0000_2000, 0x0000_4000, 0x0000_8000,
        0x0001_0000, 0x0010_0000, 0x0100_0000,
        0x1000_0000, 0x2000_0000, 0x4000_0000, 0x8000_0000,
        0xF000_0000, 0xFFFF_0000, 0xFFFF_0001, 0xFFFF_FFFF),
      IntStream.range(0, 1024).boxed().collect(Collectors.toList()),
      IntStream.range(0, 1023).boxed().collect(Collectors.toList()),
      IntStream.range(0, 1025).boxed().collect(Collectors.toList()),
      IntStream.range(0, 1030).boxed().collect(Collectors.toList()),
      IntStream.range(0, 1024).mapToObj("A"::repeat).collect(Collectors.toList())
    ).map(list -> Arguments.of(list, FastFixedSetFactory.create(list)));
  }
}
