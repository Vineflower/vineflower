package org.jetbrains.java.decompiler.util.token;

public class TextRange {
  public final int start;
  public final int length;

  public TextRange(int start, int length) {
    this.start = start;
    this.length = length;
  }

  public int getEnd() {
    return start + length;
  }

  @Override
  public String toString() {
    return String.format("%d-%d", start, getEnd());
  }
}
