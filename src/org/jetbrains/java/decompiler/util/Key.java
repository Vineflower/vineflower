package org.jetbrains.java.decompiler.util;

import java.util.Objects;

public final class Key<T> {
  public final String name;

  public Key(String name) {
    this.name = name;
  }
  
  public static <T> Key<T> of(String name) {
    return new Key<>(name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Key<?> key)) {
      return false;
    }

    return Objects.equals(name, key.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }
}
