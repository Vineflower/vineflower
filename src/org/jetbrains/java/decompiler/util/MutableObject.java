package org.jetbrains.java.decompiler.util;

public final class MutableObject<T> {
  private T value;

  private MutableObject(T value) {
    this.value = value;
  }

  public static <T> MutableObject<T> of(T value) {
    return new MutableObject<>(value);
  }

  public T get() {
    return value;
  }

  public void set(T value) {
    this.value = value;
  }
}
