package org.jetbrains.java.decompiler.api.java;

/**
 * Species where a specific Java pass should be run.
 */
public enum JavaPassLocation {
  /**
   * Runs before most resugaring passes run in the main loop.
   * Returning true has no effect.
   */
  BEFORE_MAIN(false),
  /**
   * Runs during the loop statement resugaring loop.
   * Returning true restarts the loop.
   */
  IN_LOOP_DECOMP(true),
  /**
   * Runs during the main loop, after labels have been resolved.
   * Returning true restarts the loop.
   */
  MAIN_LOOP(true),
  /**
   * Runs after the main loop has finished, before variable definitions have been resolved.
   * Returning true has no effect.
   */
  AFTER_MAIN(false),
  /**
   * Runs at the last moment to make changes to the decompiled code, after variable definitions have been resolved.
   * Returning true has no effect.
   */
  AT_END(false);

  private final boolean loop;

  JavaPassLocation(boolean loop) {
    this.loop = loop;
  }

  public boolean isLoop() {
    return loop;
  }
}
