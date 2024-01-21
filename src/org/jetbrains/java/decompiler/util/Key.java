package org.jetbrains.java.decompiler.util;

public final class Key<T> {
  public final String name;

  public Key(String name) {
    this.name = name;
  }
  
  public static <T> Key<T> of(String name) {
    return new Key<>(name);
  }
}
