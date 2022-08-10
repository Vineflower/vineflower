package org.jetbrains.java.decompiler.util;

import java.util.concurrent.ConcurrentHashMap;

public class NullableConcurrentHashMap<K,V> extends ConcurrentHashMap<K,V> {
  private final Object NULL_KEY = new Object();
  private final Object NULL_VALUE = new Object();

  @Override
  public V get(Object key) {
    if (key == null) {
      key = NULL_KEY;
    }

    V res = super.get(key);
    if (res == NULL_VALUE) {
      return null;
    }

    return res;
  }

  @Override
  public V put(K key, V value) {
    if (key == null) {
      key = (K) NULL_KEY;
    }

    if (value == null) {
      value = (V) NULL_VALUE;
    }

    return super.put(key, value);
  }
}
