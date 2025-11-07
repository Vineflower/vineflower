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
    private final FastSparseSetFactory.ArrayHolder data;
    @NotNull
    private final FastSparseSetFactory.ArrayHolder next;

    private FastSparseSet(FastSparseSetFactory<E> factory) {
      this.factory = factory;
      this.colValuesInternal = factory.getInternalValuesCollection();

      // Originally, this returned factory.getLastBlock() + 1. However, in the most common case, only 1 element is added.
      // This means that the array is unnecessarily large. Instead, max(lastBlock, 1) is used to ensure empty factories
      // don't produce -1 lengths.
      // TODO: the array init of size 1 can be elided, and the array can be lazy initialized when sized above 1
      int length = Math.max(factory.getLastBlock(), 1);
      this.data = new ArrayHolder(length);
      this.next = new ArrayHolder(length);
    }

    private FastSparseSet(FastSparseSetFactory<E> factory, ArrayHolder data, ArrayHolder next) {
      this.factory = factory;
      this.colValuesInternal = factory.getInternalValuesCollection();

      this.data = data;
      this.next = next;
    }

    public FastSparseSet<E> getCopy() {
      ArrayHolder newData = this.data.copy();
      ArrayHolder newNext = this.next.copy();

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
      for (int i = key - 1; i >= 0; i--) {
        if (getNextIdx(i) == oldnext) {
          next.set(i, newnext);
        } else {
          break;
        }
      }
    }

    public void union(FastSparseSet<E> set) {

      ArrayHolder extdata = set.getData();
      ArrayHolder intdata = data;
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
      ArrayHolder extdata = set.getData();
      ArrayHolder intdata = data;

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

      ArrayHolder extdata = set.getData();
      ArrayHolder intdata = data;
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

      ArrayHolder longdata = ((FastSparseSet)o).getData();
      ArrayHolder shortdata = data;

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
      ArrayHolder intdata = data;

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

      ArrayHolder intdata = data;

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

    private ArrayHolder getData() {
      return data;
    }

    private ArrayHolder getNext() {
      return next;
    }

    private FastSparseSetFactory<E> getFactory() {
      return factory;
    }

  public static final class FastSparseSetIterator<E> implements Iterator<E> {

    private final PackedMap<E> colValuesInternal;
    private final ArrayHolder data;
    private final ArrayHolder next;
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

  private static class ArrayHolder {
    private ArrayTower tower = ArrayTower.None.INSTANCE;
    private int size;

    public ArrayHolder(int size) {
      this.size = size;
    }

    public int get(int index) {
      return tower.get(index);
    }

    public void set(int index, int value) {
      if (!tower.canSet(index, value)) {
        ArrayTower last = tower;
        if (tower instanceof ArrayTower.None) {
          tower = new ArrayTower.Single(index, value);
        } else if (tower instanceof ArrayTower.Single single) {
          if (single.value == value) {
            // Same value with multiple indices, use bitset
            BitSet bits = new BitSet();
            bits.set(single.index);
            tower = new ArrayTower.Bits(value, bits);
          } else {
            // Different values, fall all the way down to array
            int[] ints = new int[size];
            ints[single.index] = single.value;
            tower = new ArrayTower.Array(ints);
          }
        } else if (tower instanceof ArrayTower.Bits bits) {
          int[] ints = new int[size];
          bits.promote(ints);
          tower = new ArrayTower.Array(ints);
        }

        ValidationHelper.validateTrue(last != tower, "must have changed");
      }

      tower.set(index, value);
    }

    public void resize(int newSize) {
      if (newSize > size) {
        size = newSize;

        if (tower instanceof ArrayTower.Array ary) {
          tower = new ArrayTower.Array(Arrays.copyOf(ary.ary, newSize));
        }
      }
    }

    public ArrayHolder copy() {
      ArrayHolder next = new ArrayHolder(size);
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
    }

    record Bits(int value, BitSet index) implements ArrayTower {

      @Override
      public int get(int i) {
        return index.get(i) ? value : 0;
      }

      @Override
      public void set(int i, int v) {
        ValidationHelper.assertTrue(v == value || v == 0, "must be");
        index.set(i, v == value);
      }

      @Override
      public boolean canSet(int i, int v) {
        return value == v || v == 0;
      }

      @Override
      public ArrayTower copy() {
        return new ArrayTower.Bits(value, (BitSet) index.clone());
      }

      public void promote(int[] ary) {
        for (int i = 0; i < ary.length; i++) {
          ary[i] = get(i);
        }
      }
    }

    record Array(int[] ary) implements ArrayTower {

      @Override
      public int get(int i) {
        return ary[i];
      }

      @Override
      public void set(int i, int v) {
        ary[i] = v;
      }

      @Override
      public boolean canSet(int i, int v) {
        return true;
      }

      @Override
      public ArrayTower copy() {
        return new ArrayTower.Array(Arrays.copyOf(ary, ary.length));
      }
    }
  }
}

