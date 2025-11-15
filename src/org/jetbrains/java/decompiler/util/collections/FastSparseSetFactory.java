// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.util.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;

import java.util.*;

public class FastSparseSetFactory<E> {

  private final PackedMap<E> colValuesInternal = new PackedMap<>();

  private int lastBlock;

  private int lastMask;

  public FastSparseSetFactory(Collection<? extends E> set) {

    int block = -1;
    int mask = -1;
    int index = 0;

    for (E element : set) {

      block = index >> 5;

      if ((index & 31) == 0) {
        mask = 1;
      } else {
        mask <<= 1;
      }

      colValuesInternal.putWithKey(mask, block, element);

      index++;
    }

    lastBlock = block;
    lastMask = mask;
  }

  private long addElement(E element) {
    if (lastMask < 0) {
      lastMask = 1;
      lastBlock++;
    } else {
      lastMask <<= 1;
    }

    return colValuesInternal.putWithKey(lastMask, lastBlock, element);
  }

  public FastSparseSet<E> createEmptySet() {
    return new FastSparseSet<>(this);
  }

  private int getLastBlock() {
    return lastBlock;
  }

  private PackedMap<E> getInternalValuesCollection() {
    return colValuesInternal;
  }


  public static final class FastSparseSet<E> implements Iterable<E> {
    public static final FastSparseSet[] EMPTY_ARRAY = new FastSparseSet[0];

    private final FastSparseSetFactory<E> factory;

    private final PackedMap<E> colValuesInternal;

    @NotNull
    private final FastSparseSetFactory.IntArray data;
    @NotNull
    private final FastSparseSetFactory.IntArray next;

    private FastSparseSet(FastSparseSetFactory<E> factory) {
      this.factory = factory;
      this.colValuesInternal = factory.getInternalValuesCollection();

      // Originally, this returned factory.getLastBlock() + 1. However, in the most common case, only 1 element is added.
      // This means that the array is unnecessarily large. Instead, max(lastBlock, 1) is used to ensure empty factories
      // don't produce -1 lengths.
      int length = Math.max(factory.getLastBlock(), 1);
      this.data = new IntArray(length);
      this.next = new IntArray(length);
    }

    private FastSparseSet(FastSparseSetFactory<E> factory, IntArray data, IntArray next) {
      this.factory = factory;
      this.colValuesInternal = factory.getInternalValuesCollection();

      this.data = data;
      this.next = next;
    }

    public FastSparseSet<E> getCopy() {
      IntArray newData = this.data.copy();
      IntArray newNext = this.next.copy();

      return new FastSparseSet<>(factory, newData, newNext);
    }

    private void ensureCapacity(int index) {

      int newlength = data.length();
      if (newlength == 0) {
        newlength = 1;
      }

      while (newlength <= index) {
        newlength *= 2;
      }

      data.resize(newlength);
      next.resize(newlength);
    }

    public void add(E element) {
      long index;
      if (!colValuesInternal.containsKey(element)) {
        index = factory.addElement(element);
      } else {
        index = colValuesInternal.getWithKey(element);
      }

      int block = PackedMap.unpackLow(index);
      if (block >= data.length()) {
        ensureCapacity(block);
      }

      data.set(block, data.get(block) | PackedMap.unpackHigh(index));

      changeNext(block, getNextIdx(block), block);
    }

    private int getNextIdx(int block) {
      return next.get(block);
    }

    public void remove(E element) {
      long index;
      if (!colValuesInternal.containsKey(element)) {
        index = factory.addElement(element);
        // TODO: if the element isn't in the map yet, why does it need to be removed
      } else {
        index = colValuesInternal.getWithKey(element);
      }

      int block = PackedMap.unpackLow(index);
      if (block < data.length()) {
        data.set(block, data.get(block) & ~PackedMap.unpackHigh(index));

        if (data.get(block) == 0) {
          changeNext(block, block, getNextIdx(block));
        }
      }
    }

    public boolean contains(E element) {
      long index;
      if (!colValuesInternal.containsKey(element)) {
        index = factory.addElement(element);
        // TODO: if the element isn't in the map yet, how can it be contained
      } else {
        index = colValuesInternal.getWithKey(element);
      }

      int block = PackedMap.unpackLow(index);
      return block < data.length() && ((data.get(block) & PackedMap.unpackHigh(index)) != 0);
    }

    private void setNext() {

      int link = 0;
      for (int i = data.length() - 1; i >= 0; i--) {
        if (link != 0) {
          next.set(i, link);
        }

        if (data.get(i) != 0) {
          link = i;
        }
      }
    }

    private void changeNext(int key, int oldnext, int newnext) {
      if (oldnext == newnext) {
        return;
      }

      for (int i = key - 1; i >= 0; i--) {
        if (getNextIdx(i) == oldnext) {
          next.set(i, newnext);
        } else {
          break;
        }
      }
    }

    public void union(FastSparseSet<E> set) {

      IntArray extdata = set.getData();
      IntArray intdata = data;
      int intlength = intdata.length();

      int pointer = 0;
      do {
        if (pointer >= intlength) {
          ensureCapacity(extdata.length() - 1);
        }

        boolean nextrec = (intdata.get(pointer) == 0);
        intdata.set(pointer, intdata.get(pointer) | extdata.get(pointer));

        if (nextrec) {
          changeNext(pointer, getNextIdx(pointer), pointer);
        }

        pointer = set.getNextIdx(pointer);
      }
      while (pointer != 0);
    }

    public void intersection(FastSparseSet<E> set) {
      IntArray extdata = set.getData();
      IntArray intdata = data;

      int minlength = Math.min(extdata.length(), intdata.length());

      for (int i = minlength - 1; i >= 0; i--) {
        intdata.set(i, intdata.get(i) & extdata.get(i));
      }

      for (int i = intdata.length() - 1; i >= minlength; i--) {
        intdata.set(i, 0);
      }

      setNext();
    }

    public void complement(FastSparseSet<E> set) {

      IntArray extdata = set.getData();
      IntArray intdata = data;
      int extlength = extdata.length();

      int pointer = 0;
      do {
        if (pointer >= extlength) {
          break;
        }

        intdata.set(pointer, intdata.get(pointer) & ~extdata.get(pointer));
        if (intdata.get(pointer) == 0) {
          changeNext(pointer, pointer, getNextIdx(pointer));
        }

        pointer = getNextIdx(pointer);
      }
      while (pointer != 0);
    }

    @Override
    public int hashCode() {
      return toPlainSet().hashCode();
    }

    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof FastSparseSet)) return false;

      IntArray longdata = ((FastSparseSet)o).getData();
      IntArray shortdata = data;

      if (data.length() > longdata.length()) {
        shortdata = longdata;
        longdata = data;
      }

      for (int i = shortdata.length() - 1; i >= 0; i--) {
        if (shortdata.get(i) != longdata.get(i)) {
          return false;
        }
      }

      for (int i = longdata.length() - 1; i >= shortdata.length(); i--) {
        if (longdata.get(i) != 0) {
          return false;
        }
      }

      return true;
    }

    public int getCardinality() {

      boolean found = false;
      IntArray intdata = data;

      for (int i = intdata.length() - 1; i >= 0; i--) {
        int block = intdata.get(i);
        if (block != 0) {
          if (found) {
            return 2;
          } else {
            if ((block & (block - 1)) == 0) {
              found = true;
            } else {
              return 2;
            }
          }
        }
      }

      return found ? 1 : 0;
    }

    public boolean isEmpty() {
      return data.length() == 0 || (getNextIdx( 0) == 0 && data.get(0) == 0);
    }

    @Override
    public Iterator<E> iterator() {
      return new FastSparseSetIterator<>(this);
    }

    public Set<E> toPlainSet() {
      HashSet<E> set = new HashSet<>();

      IntArray intdata = data;

      int size = data.length() * 32;
      if (size > colValuesInternal.size()) {
        size = colValuesInternal.size();
      }

      for (int i = size - 1; i >= 0; i--) {
        long index = colValuesInternal.get(i);

        int lo = PackedMap.unpackLow(index);
        if ((intdata.get(lo) & PackedMap.unpackHigh(index)) != 0) {
          set.add(colValuesInternal.getKey(i));
        }
      }

      return set;
    }

    public String toString() {
      return toPlainSet().toString();
    }

    private IntArray getData() {
      return data;
    }

    private IntArray getNext() {
      return next;
    }

    private FastSparseSetFactory<E> getFactory() {
      return factory;
    }

  public static final class FastSparseSetIterator<E> implements Iterator<E> {

    private final PackedMap<E> colValuesInternal;
    private final IntArray data;
    private final IntArray next;
    private final int size;

    private int pointer = -1;
    private int next_pointer = -1;

    private FastSparseSetIterator(FastSparseSet<E> set) {
      colValuesInternal = set.getFactory().getInternalValuesCollection();
      data = set.getData();
      next = set.getNext();
      size = colValuesInternal.size();
    }

    private int getNextIndex(int index) {

      index++;
      int bindex = index >>> 5;
      int dindex = index & 0x1F;

      while (bindex < data.length()) {
        int block = data.get(bindex);

        if (block != 0) {
          block >>>= dindex;
          while (dindex < 32) {
            if ((block & 1) != 0) {
              return (bindex << 5) + dindex;
            }
            block >>>= 1;
            dindex++;
          }
        }

        dindex = 0;
        bindex = next.get(bindex);

        if (bindex == 0) {
          break;
        }
      }

      return -1;
    }

    @Override
    public boolean hasNext() {
      next_pointer = getNextIndex(pointer);
      return (next_pointer >= 0);
    }

    @Override
    public E next() {
      if (next_pointer >= 0) {
        pointer = next_pointer;
      }
      else {
        pointer = getNextIndex(pointer);
        if (pointer == -1) {
          pointer = size;
        }
      }

      next_pointer = -1;
      return pointer < size ? colValuesInternal.getKey(pointer) : null;
    }

    @Override
    public void remove() {
      long index = colValuesInternal.get(pointer);
      int lo = PackedMap.unpackLow(index);
      data.set(lo, data.get(lo) & ~PackedMap.unpackHigh(index));
    }
  }

  }

  public static final class IntArray {
    private ArrayTower tower = ArrayTower.None.INSTANCE;
    private int size;

    public IntArray(int size) {
      this.size = size;
    }

    public void setNone() {
      tower = ArrayTower.None.INSTANCE;
    }

    public String str() {
      return tower.toString();
    }

    public int get(int index) {
      return tower.get(index);
    }

    public void set(int index, int value) {
      if (!tower.canSet(index, value)) {
        ArrayTower last = tower;
        if (tower instanceof ArrayTower.None) {
          if (index == 0 && value == 1) {
            tower = new ArrayTower.Ladder();
          } else {
            tower = new ArrayTower.Single(index, value);
          }
        } else if (tower instanceof ArrayTower.Single single) {
          if (single.value == value && (single.index == index + 1 || single.index == index - 1)) {
            // Same value with multiple indices, use bitset
//            BitSet bits = new BitSet();
//            bits.set(single.index);
//            tower = new ArrayTower.Bits(value, bits);

            // range
            tower = new ArrayTower.Range(value, single.index, single.index);
          } else {
            // Different values, fall all the way down to array
            if (index == single.index || single.value == 0) {
              // Size is one, just replace the value in single
              tower = new ArrayTower.Single(index, value);
            } else {
              int[] ints = new int[size];
              ints[single.index] = single.value;
              tower = new ArrayTower.Array(ints, 1);
            }
          }
        }
//        else if (tower instanceof ArrayTower.Bits bits) {
//          int[] ints = new int[size];
//          bits.promote(ints);
//          tower = new ArrayTower.Array(ints, bits.set);
//        }
        else if (tower instanceof ArrayTower.Ladder ladder) {
          int[] ints = new int[size];
          ladder.promote(ints);
          tower = new ArrayTower.Array(ints, ladder.size + 1);
        } else if (tower instanceof ArrayTower.Range range) {
          int[] ints = new int[size];
          range.promote(ints);
          tower = new ArrayTower.Array(ints, (range.end - range.start) + 1);
        }

        ValidationHelper.validateTrue(last != tower, "must have changed");
      }

      tower.set(index, value);

//      if (this.tower instanceof ArrayTower.Bits bits && bits.set == 0) {
//        this.tower = ArrayTower.None.INSTANCE;
//      } else
      if (this.tower instanceof ArrayTower.Array ary) {
        if (ary.set == 0) {
          this.tower = ArrayTower.None.INSTANCE;
        }
      } else if (this.tower instanceof ArrayTower.Range range) {
        if (range.end < range.start) {
          this.tower = ArrayTower.None.INSTANCE;
        } else if (range.end == range.start) {
          this.tower = new ArrayTower.Single(range.start, range.value);
        }
      }
    }

    public void resize(int newSize) {
      if (newSize > size) {
        size = newSize;

        if (tower instanceof ArrayTower.Array ary) {
          tower = new ArrayTower.Array(Arrays.copyOf(ary.ary, newSize), ary.set);
        }
      }
    }

    public int cardApprox() {
      if (this.tower instanceof ArrayTower.None) {
        return 0;
      } else if (this.tower instanceof ArrayTower.Single) {
        return 1;
      } else if (this.tower instanceof ArrayTower.Bits bits) {
        return Math.min(bits.set, 2);
      } else if (this.tower instanceof ArrayTower.Ladder ladder) {
        return Math.min(ladder.size + 1, 2);
      } else if (this.tower instanceof ArrayTower.Array array) {
        return Math.min(array.set, 2);
      }

      throw new IllegalStateException("Illegal state!");
    }

    public IntArray copy() {
      IntArray next = new IntArray(size);
      next.tower = tower.copy();

      return next;
    }

    public int length() {
      return size;
    }
  }

  private sealed interface ArrayTower {
    int get(int i);

    default void set(int i, int v) {
      if (canSet(i, v)) {
        return;
      }

      throw new IllegalStateException("Can't set " + i + " " + v);
    }

    boolean canSet(int i, int v);

    ArrayTower copy();

    default void promote(int[] ary) {
      for (int i = 0; i < ary.length; i++) {
        ary[i] = get(i);
      }
    }

    record None() implements ArrayTower {
      public static final None INSTANCE = new None();

      @Override
      public int get(int i) {
        return 0;
      }

      @Override
      public boolean canSet(int i, int v) {
        return v == 0;
      }

      @Override
      public ArrayTower copy() {
        return INSTANCE;
      }

      @Override
      public String toString() {
        return "Nil";
      }
    }

    record Single(int index, int value) implements ArrayTower {

      @Override
      public int get(int i) {
        return i == index ? value : 0;
      }

      @Override
      public boolean canSet(int i, int v) {
        return (i == index && v == value) || (i != index && v == 0);
      }

      @Override
      public ArrayTower copy() {
        return new Single(index, value);
      }

      @Override
      public String toString() {
        return value + "@" + index;
      }
    }

    final class Ladder implements ArrayTower {
      private int size = -1;

      @Override
      public int get(int i) {
        return i <= size ? i + 1 : 0;
      }

      @Override
      public void set(int i, int v) {
        // Increase ladder size
        if (i == size + 1) {
          if (v == size + 2) {
            size++;
          } else if (v == 0) {
            size--;
          } else {
            ValidationHelper.assertTrue(false, "impossible case");
          }
        }
        // else already in ladder, no change
      }

      @Override
      public boolean canSet(int i, int v) {
        return i == size + 1 && (v == size + 2 || v == 0) // new in ladder
          || i <= size && i + 1 == v // already in ladder
          ;
      }

      @Override
      public ArrayTower copy() {
        Ladder ladder = new Ladder();
        ladder.size = size;
        return ladder;
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(i + 1);
        }
        return "Ladder:" + sb.toString();
      }
    }

    final class Bits implements ArrayTower {
      private final int value;
      private final BitSet index;
      private int set = 0;

      private Bits(int value, BitSet index) {
        this.value = value;
        this.index = index;
      }

      @Override
      public int get(int i) {
        return index.get(i) ? value : 0;
      }

      @Override
      public void set(int i, int v) {
        ValidationHelper.assertTrue(v == value || v == 0, "must be");
        int old = index.get(i) ? value : 0;

        index.set(i, v == value);

        if (old == 0 && v != 0) {
          set++;
        } else if (old != 0 && v == 0) {
          set--;
        }
      }

      @Override
      public boolean canSet(int i, int v) {
        return value == v || v == 0;
      }

      @Override
      public ArrayTower copy() {
        Bits bits = new Bits(value, (BitSet) index.clone());
        bits.set = set;
        return bits;
      }

      @Override
      public String toString() {
        return value + "@" + index;
      }
    }

    final class Range implements ArrayTower {
      private final int value;
      private int start;
      private int end;

      public Range(int value, int start, int end) {
        this.value = value;
        this.start = start;
        this.end = end;
      }

      @Override
      public int get(int i) {
        return i >= start && i <= end ? value : 0;
      }

      @Override
      public void set(int i, int v) {
        if (v == 0) {
          // contract range
          if (i == start) {
            start++;
          } else {
            end--;
          }
        } else {
          // expand range
          if (i == start - 1) {
            start--;
          } else {
            end++;
          }
        }
      }

      @Override
      public boolean canSet(int i, int v) {
        // TODO: extract common behavior?
        if (v == value) {
          // expand range
          if (i == start - 1) {
            return true;
          } else if (i == end + 1) {
            return true;
          }
        } else if (v == 0) {
          // contract range
          if (i == start) {
            return true;
          } else if (i == end) {
            return true;
          }
        }

        return false;
      }

      @Override
      public ArrayTower copy() {
        return new Range(value, start, end);
      }

      @Override
      public String toString() {
        return value + "@[" + start + "-" + end + "]";
      }
    }

    final class Array implements ArrayTower {
      private final int[] ary;
      private int set;

      private Array(int[] ary, int set) {
        this.ary = ary;
        this.set = set;
      }

      @Override
      public int get(int i) {
        return ary[i];
      }

      @Override
      public void set(int i, int v) {
        int old = ary[i];
        ary[i] = v;

        if (old == 0 && v != 0) {
          set++;
        } else if (old != 0 && v == 0) {
          set--;
        }
      }

      @Override
      public boolean canSet(int i, int v) {
        return true;
      }

      @Override
      public ArrayTower copy() {
        return new ArrayTower.Array(Arrays.copyOf(ary, ary.length), set);
      }

      @Override
      public String toString() {
        return Arrays.toString(ary);
      }
    }
  }
}

