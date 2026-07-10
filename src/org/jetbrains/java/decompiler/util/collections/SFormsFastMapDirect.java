// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.util.collections;

import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionNode;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory.FastSparseSet;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory.IntArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

// Three part map to hold live var version data for specific variables.
// Segmented to handle the cases of true variable (index 0), stack variable (index 1), and field access (index 2) separately as they can have the same index.
public class SFormsFastMapDirect {

  private int size;
  private final FastSparseSetFactory<Integer> factory;

  private final FastSparseSetArray[] elements = new FastSparseSetArray[3];

  private final IntArray[] next = new IntArray[3];

  public SFormsFastMapDirect(FastSparseSetFactory<Integer> factory) {
    this(true, factory);
  }

  private SFormsFastMapDirect(boolean initialize, FastSparseSetFactory<Integer> factory) {
    this.factory = factory;
    if (initialize) {
      for (int i = 2; i >= 0; i--) {
        elements[i] = new FastSparseSetArray(0);
        next[i] = new FastSparseSetFactory.IntArray(0);
      }
    }
  }

  public SFormsFastMapDirect getCopy() {

    SFormsFastMapDirect map = new SFormsFastMapDirect(false, factory);
    map.size = size;

    FastSparseSetArray[] mapelements = map.elements;
    IntArray[] mapnext = map.next;

    for (int i = 2; i >= 0; i--) {
      FastSparseSetArray arr = elements[i];
      int length = arr.length();

      if (length > 0) {
        IntArray arrnext = next[i];

        mapelements[i] = arr.copy();
        mapnext[i] = arrnext.copy();

      } else {
        mapelements[i] = new FastSparseSetArray(0);
        mapnext[i] = new IntArray(0);
      }
    }

    return map;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public void put(int key, FastSparseSet<Integer> value) {
    putInternal(key, value, false);
  }

  public void remove(int key) {
    putInternal(key, null, true);
  }

  public void removeAllFields() {
    FastSparseSetArray arr = elements[2];
    IntArray arrnext = next[2];

    size -= arr.cardinality();

    arrnext.setNone();
    arr.setNone();
  }

  public void removeAllStacks() {
    FastSparseSetArray arr = elements[1];
    IntArray arrnext = next[1];

    size -= arr.cardinality();

    arrnext.setNone();
    arr.setNone();
  }

  private void putInternal(final int key, final FastSparseSet<Integer> value, boolean remove) {
    int index = 0;
    int ikey = key;
    if (ikey < 0) {
      index = 2;
      ikey = -ikey;
    } else if (ikey >= VarExprent.STACK_BASE) {
      index = 1;
      ikey -= VarExprent.STACK_BASE;
    }

    FastSparseSetArray arr = elements[index];
    if (ikey >= arr.length()) {
      if (remove) {
        return;
      } else {
        ensureCapacity(index, ikey + 1, false);
      }
    }

    FastSparseSet<Integer> oldval = arr.get(ikey);
    arr.set(ikey, value);

    IntArray arrnext = next[index];

    if (oldval == null && value != null) {
      size++;
      changeNext(arrnext, ikey, arrnext.get(ikey), ikey);
    } else if (oldval != null && value == null) {
      size--;
      changeNext(arrnext, ikey, ikey, arrnext.get(ikey));
    }
  }

  private static void changeNext(IntArray arrnext, int key, int oldnext, int newnext) {
    if (oldnext == newnext) {
      return;
    }
    for (int i = key - 1; i >= 0; i--) {
      if (arrnext.get(i) == oldnext) {
        arrnext.set(i, newnext);
      } else {
        break;
      }
    }
  }

  public boolean containsKey(int key) {
    return get(key) != null;
  }

  public FastSparseSet<Integer> get(int key) {

    int index = 0;
    if (key < 0) {
      index = 2;
      key = -key;
    } else if (key >= VarExprent.STACK_BASE) {
      index = 1;
      key -= VarExprent.STACK_BASE;
    }

    FastSparseSetArray arr = elements[index];

    if (key < arr.length()) {
      return arr.get(key);
    }

    return null;
  }

  public void complement(SFormsFastMapDirect map) {

    for (int i = 2; i >= 0; i--) {
      FastSparseSetArray lstOwn = elements[i];

      if (lstOwn.length() == 0) {
        continue;
      }

      FastSparseSetArray lstExtern = map.elements[i];
      IntArray arrnext = next[i];

      int pointer = 0;
      do {
        FastSparseSet<Integer> first = lstOwn.get(pointer);

        if (first != null) {
          if (pointer >= lstExtern.length()) {
            break;
          }
          FastSparseSet<Integer> second = lstExtern.get(pointer);

          if (second != null) {
            first.complement(second);
            if (first.isEmpty()) {
              lstOwn.set(pointer, null);
              size--;
              changeNext(arrnext, pointer, pointer, arrnext.get(pointer));
            }
          }
        }

        pointer = arrnext.get(pointer);
      }
      while (pointer != 0);
    }
  }

  public void intersection(SFormsFastMapDirect map) {

    for (int i = 2; i >= 0; i--) {
      FastSparseSetArray lstOwn = elements[i];

      if (lstOwn.length() == 0) {
        continue;
      }

      FastSparseSetArray lstExtern = map.elements[i];
      IntArray arrnext = next[i];

      int pointer = 0;
      do {
        FastSparseSet<Integer> first = lstOwn.get(pointer);

        if (first != null) {
          FastSparseSet<Integer> second = null;
          if (pointer < lstExtern.length()) {
            second = lstExtern.get(pointer);
          }

          if (second != null) {
            first.intersection(second);
          }

          if (second == null || first.isEmpty()) {
            lstOwn.set(pointer, null);
            size--;
            changeNext(arrnext, pointer, pointer, arrnext.get(pointer));
          }
        }

        pointer = arrnext.get(pointer);
      }
      while (pointer != 0);
    }
  }

  public void union(SFormsFastMapDirect map) {

    for (int i = 2; i >= 0; i--) {
      FastSparseSetArray lstExtern = map.elements[i];

      if (lstExtern.length() == 0) {
        continue;
      }

      FastSparseSetArray lstOwn = elements[i];

      IntArray arrnext = next[i];
      IntArray arrnextExtern = map.next[i];

      int pointer = 0;
      do {
        if (pointer >= lstOwn.length()) {
          ensureCapacity(i, lstExtern.length(), true);
          arrnext = next[i];
        }

        FastSparseSet<Integer> second = lstExtern.get(pointer);

        if (second != null) {
          FastSparseSet<Integer> first = lstOwn.get(pointer);

          if (first == null) {
            lstOwn.set(pointer, second.getCopy());
            size++;
            changeNext(arrnext, pointer, arrnext.get(pointer), pointer);
          }
          else {
            first.union(second);
          }
        }

        pointer = arrnextExtern.get(pointer);
      }
      while (pointer != 0);
    }
  }

  public String toString() {

    StringBuilder buffer = new StringBuilder("{");

    List<Entry<Integer, FastSparseSet<Integer>>> lst = entryList();
    if (lst != null) {
      boolean first = true;
      for (Entry<Integer, FastSparseSet<Integer>> entry : lst) {
        if (!first) {
          buffer.append(", ");
        }
        else {
          first = false;
        }

        Set<Integer> set = entry.getValue().toPlainSet();
        buffer.append(entry.getKey()).append("={").append(set.toString()).append("}");
      }
    }

    buffer.append("}");
    return buffer.toString();
  }

  public List<Entry<Integer, FastSparseSet<Integer>>> entryList() {
    List<Entry<Integer, FastSparseSet<Integer>>> list = new ArrayList<>();

    for (int i = 2; i >= 0; i--) {
      int ikey = 0;
      for (int j = 0; j < elements[i].length(); j++) {
        final FastSparseSet<Integer> ent = elements[i].get(j);
        if (ent != null) {
          final int key = i == 0 ? ikey : (i == 1 ? ikey + VarExprent.STACK_BASE : -ikey);

          list.add(new Entry<Integer, FastSparseSet<Integer>>() {

            @Override
            public Integer getKey() {
              return key;
            }

            @Override
            public FastSparseSet<Integer> getValue() {
              return ent;
            }

            @Override
            public FastSparseSet<Integer> setValue(FastSparseSet<Integer> newvalue) {
              return null;
            }
          });
        }

        ikey++;
      }
    }

    return list;
  }

  private void ensureCapacity(int index, int size, boolean exact) {

    FastSparseSetArray arr = elements[index];
    IntArray arrnext = next[index];

    int minsize = size;
    if (!exact) {
      minsize = 2 * arr.length() / 3 + 1;
      if (size > minsize) {
        minsize = size;
      }
    }

    arr.resize(minsize);
    arrnext.resize(minsize);
  }

  public void setCurrentVar(int var, int version) {
    FastSparseSet<Integer> set = this.factory.createEmptySet();
    set.add(version);
    this.put(var, set);
  }

  public void setCurrentVar(VarExprent varExprent) {
    this.setCurrentVar(varExprent.getIndex(), varExprent.getVersion());
  }

  public void setCurrentVar(VarVersionNode varExprent) {
    this.setCurrentVar(varExprent.var, varExprent.version);
  }

  public void setCurrentVar(VarVersionPair varExprent) {
    this.setCurrentVar(varExprent.var, varExprent.version);
  }

  public FastSparseSet<Integer> get(VarExprent varExprent) {
    return this.get(varExprent.getIndex());
  }

  private static class FastSparseSetArray {
    private ArrayTower tower = ArrayTower.None.INSTANCE;
    private int size;

    public FastSparseSetArray(int size) {
      this.size = size;
    }

    public FastSparseSet<Integer> get(int index) {
      return tower.get(index);
    }

    public void setNone() {
      tower = ArrayTower.None.INSTANCE;
    }

    public void set(int index, FastSparseSet<Integer> value) {
      if (!tower.canSet(index, value)) {
        ArrayTower last = tower;
        if (tower instanceof ArrayTower.None) {
          tower = new ArrayTower.Single(index, value);
        } else if (tower instanceof ArrayTower.Single single) {
          // Different values, fall all the way down to array
          if (index == single.index || single.value == null) {
            // Size is one, just replace the value in single
            tower = new ArrayTower.Single(index, value);
          } else {
            FastSparseSet<Integer>[] ints = new FastSparseSet[size];
            ints[single.index] = single.value;
            tower = new ArrayTower.Array(ints, 1);
          }
        }

        ValidationHelper.validateTrue(last != tower, "must have changed");
      }

      tower.set(index, value);

      if (tower instanceof ArrayTower.Array ary && ary.set == 0) {
        tower = ArrayTower.None.INSTANCE;
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

    public int cardinality() {
      if (this.tower instanceof ArrayTower.None) {
        return 0;
      } else if (this.tower instanceof ArrayTower.Single) {
        return 1;
      } else if (this.tower instanceof ArrayTower.Array ary) {
        return ary.set;
      }

      throw new IllegalStateException("illegal state!");
    }

    public FastSparseSetArray copy() {
      FastSparseSetArray next = new FastSparseSetArray(size);
      next.tower = tower.copy();

      return next;
    }

    public int length() {
      return size;
    }
  }

  private sealed interface ArrayTower {
    FastSparseSet<Integer> get(int i);

    default void set(int i, FastSparseSet<Integer> v) {
      if (canSet(i, v)) {
        return;
      }

      throw new IllegalStateException("Can't set " + i + " " + v);
    }

    boolean canSet(int i, FastSparseSet<Integer> v);

    ArrayTower copy();

    record None() implements ArrayTower {
      public static final None INSTANCE = new None();

      @Override
      public FastSparseSet<Integer> get(int i) {
        return null;
      }

      @Override
      public boolean canSet(int i, FastSparseSet<Integer> v) {
        return v == null;
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

    record Single(int index, FastSparseSet<Integer> value) implements ArrayTower {

      @Override
      public FastSparseSet<Integer> get(int i) {
        return i == index ? value : null;
      }

      @Override
      public boolean canSet(int i, FastSparseSet<Integer> v) {
        return (i == index && v == value) || (i != index && v == null);
      }

      @Override
      public ArrayTower copy() {
        return new Single(index, value.getCopy());
      }

      @Override
      public String toString() {
        return value + "@" + index;
      }
    }

    final class Array implements ArrayTower {
      private final FastSparseSet<Integer>[] ary;
      private int set;

      Array(FastSparseSet<Integer>[] ary, int set) {
        this.ary = ary;
        this.set = set;
      }

      @Override
      public FastSparseSet<Integer> get(int i) {
        return ary[i];
      }

      @Override
      public void set(int i, FastSparseSet<Integer> v) {
        FastSparseSet<Integer> old = ary[i];
        ary[i] = v;

        if (old == null && v != null) {
          set++;
        } else if (old != null && v == null) {
          set--;
        }
      }

      @Override
      public boolean canSet(int i, FastSparseSet<Integer> v) {
        return true;
      }

      @Override
      public ArrayTower copy() {
        FastSparseSet<Integer>[] cpy = Arrays.copyOf(ary, ary.length);
        for (int i = 0; i < cpy.length; i++) {
          FastSparseSet<Integer> v = cpy[i];
          cpy[i] = v == null ? null : v.getCopy();
        }

        return new ArrayTower.Array(cpy, set);
      }

      @Override
      public String toString() {
        return Arrays.toString(ary);
      }
    }
  }
}