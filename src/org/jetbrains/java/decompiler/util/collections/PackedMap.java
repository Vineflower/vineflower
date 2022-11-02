package org.jetbrains.java.decompiler.util.collections;

import java.util.HashMap;
import java.util.Map;

/**
 * Limited sub-implementation of VBStyleCollection that only supports putWithKey and getWithKey.
 * Error checking is mostly not implemented, it is up to the caller to validate the input.
 * For use with FastSparseSetFactory.
 *
 * @param <K> key type
 */
final class PackedMap<K> {
  private long[] values = new long[16];
  private Object[] keys = new Object[16];
  // TODO: is there a way to improve this to not use boxed indices?
  private final Map<K, Integer> mapKeys = new HashMap<>();
  private int size = 0;

  public long putWithKey(int high, int low, K key) {
    long packed = PackedMap.pack(high, low);
    this.putWithKey(packed, key);
    return packed;
  }

  public void putWithKey(long element, K key) {
    Integer index = mapKeys.get(key);

    if (index != null) {
      int iIndex = index;

      values[iIndex] = element;
    } else {
      int iIndex = size;
      if (iIndex >= values.length) {
        resize();
      }

      values[iIndex] = element;
      keys[iIndex] = key;
      mapKeys.put(key, iIndex);

      size++;
    }
  }

  public boolean containsKey(K key) {
    return mapKeys.containsKey(key);
  }

  public long getWithKey(K key) {
    Integer integer = mapKeys.get(key);
    if (integer == null) {
      throw new IllegalArgumentException("Key not found: " + key + " in key map. Use containsKey to check if it exists first!");
    }

    return values[integer];
  }

  public long get(int index) {
    return values[index];
  }

  public K getKey(int index) {
    return (K) keys[index];
  }

  private void resize() {
    long[] newVals = new long[size + (size / 2)];
    Object[] newKeys = new Object[size + (size / 2)];

    System.arraycopy(values, 0, newVals, 0, size);
    System.arraycopy(keys, 0, newKeys, 0, size);

    values = newVals;
    keys = newKeys;
  }

  public int size() {
    return size;
  }

  public static long pack(int high, int low) {
    return (long)low & 0xffffffffL | ((long)high & 0xffffffffL) << 32;
  }

  public static int unpackLow(long packed) {
    return (int)(packed & 0xffffffffL);
  }

  public static int unpackHigh(long packed) {
    return (int)((packed >>> 32) & 0xffffffffL);
  }
}
