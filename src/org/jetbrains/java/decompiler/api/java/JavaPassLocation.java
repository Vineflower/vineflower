package org.jetbrains.java.decompiler.api.java;

public enum JavaPassLocation {
  BEFORE_MAIN(false),
  IN_LOOP_DECOMP(true),
  MAIN_LOOP(true),
  AFTER_MAIN(false),
  AT_END(false);

  private final boolean loop;

  JavaPassLocation(boolean loop) {
    this.loop = loop;
  }

  public boolean isLoop() {
    return loop;
  }
}
