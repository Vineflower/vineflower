package org.jetbrains.java.decompiler.struct.gen;

import org.jetbrains.annotations.NotNull;

public enum TypeFamily {
  UNKNOWN,
  BOOLEAN,
  INTEGER,
  FLOAT,
  LONG,
  DOUBLE,
  OBJECT;

  // TODO: document what these mean, and try to remove! Doesn't make sense to have these

  public boolean isGreater(@NotNull TypeFamily other) {
    return ordinal() > other.ordinal();
  }

  public boolean isLesser(@NotNull TypeFamily other) {
    return ordinal() < other.ordinal();
  }

  public boolean isLesserOrEqual(@NotNull TypeFamily other) {
    return ordinal() <= other.ordinal();
  }
}
