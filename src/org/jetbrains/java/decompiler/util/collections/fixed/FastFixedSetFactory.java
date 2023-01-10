package org.jetbrains.java.decompiler.util.collections.fixed;

import java.util.Collection;

public abstract class FastFixedSetFactory<E> {
  @Deprecated
  public abstract FastFixedSet<E> spawnEmptySet();

  public abstract Collection<? extends E> getEntries();

  public static <E>FastFixedSetFactory<E> create(Collection<E> entries){
    if(entries.size() <= 64){
      return new ShortFastFixedSetFactory<>(entries);
    } else {
      return new LongFastFixedSetFactory<>(entries);
    }
  }

  public FastFixedSet<E> createEmptySet() {
    return this.spawnEmptySet();
  }

  public FastFixedSet<E> createCopiedSet() {
    FastFixedSet<E> set = this.spawnEmptySet();
    set.setAllElements();
    return set;
  }
}
