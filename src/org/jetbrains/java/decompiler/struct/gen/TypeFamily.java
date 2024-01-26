package org.jetbrains.java.decompiler.struct.gen;

import org.jetbrains.annotations.NotNull;

public enum TypeFamily {
  UNKNOWN,
  BOOLEAN,
  INTEGER(true),
  FLOAT(true),
  LONG(true),
  DOUBLE(true),
  OBJECT;

  private final boolean numeric;

  TypeFamily() {
    this(false);
  }

  TypeFamily(boolean numeric) {
    this.numeric = numeric;
  }

  public boolean isNumeric() {
    return numeric;
  }

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
