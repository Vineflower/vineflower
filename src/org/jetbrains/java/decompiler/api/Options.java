package org.jetbrains.java.decompiler.api;

import java.util.HashMap;
import java.util.Map;

public final class Options {
  private final Map<Option<?>, Object> values = new HashMap<>();

  public <T> Options with(Option<T> option, T value) {
    if (option.isValid.test(value)) {
      this.values.put(option, value);
    } else {
      throw new IllegalArgumentException("Value " + value + " is invalid for " + option.longName);
    }
    return this;
  }

  public void putAll(Options options) {
    this.values.putAll(options.values);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Option<T> option) {
    return (T) this.values.getOrDefault(option, option.defaultValue);
  }
}
