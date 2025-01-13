package org.jetbrains.java.decompiler.util;

import java.util.Optional;
import java.util.function.Consumer;

public final class Either<L, R> {
  private final L left;
  private final R right;

  private Either(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> Either<L, R> left(L left) {
    if (left == null) {
      throw new IllegalStateException("Left cannot be null");
    }

    return new Either<>(left, null);
  }

  public static <L, R> Either<L, R> right(R right) {
    if (right == null) {
      throw new IllegalStateException("Right cannot be null");
    }

    return new Either<>(null, right);
  }

  public void map(Consumer<L> ifLeft, Consumer<R> ifRight) {
    checkInvariants();

    if (left != null) {
      ifLeft.accept(left);
    }

    if (right != null) {
      ifRight.accept(right);
    }
  }

  public Optional<L> left() {
    checkInvariants();

    return Optional.ofNullable(left);
  }

  public Optional<R> right() {
    checkInvariants();

    return Optional.ofNullable(right);
  }

  private void checkInvariants() {
    if (left == null && right == null) {
      throw new IllegalStateException("Either is empty!");
    }

    if (left != null && right != null) {
      throw new IllegalStateException("Either is full!");
    }
  }
}
