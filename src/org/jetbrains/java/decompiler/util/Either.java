package org.jetbrains.java.decompiler.util;

import java.util.function.Consumer;

public final class Either<L, R> {
  private final L left;
  private final R right;

  private Either(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> Either<L, R> left(L left) {
    return new Either<>(left, null);
  }

  public static <L, R> Either<L, R> right(R right) {
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

  private void checkInvariants() {
    if (left == null && right == null) {
      throw new IllegalStateException("Either is empty!");
    }

    if (left != null && right != null) {
      throw new IllegalStateException("Either is full!");
    }
  }
}
