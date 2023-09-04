package org.jetbrains.java.decompiler.api.plugin.pass;

/**
 * A pass that can be run on the decompiled code.
 */
@FunctionalInterface
public interface Pass {
  Pass NO_OP = ctx -> false;

  /**
   * Runs this pass on the given decompiled code.
   *
   * @param ctx The decompiled code context
   * @return Whether the decompiled code was modified
   */

  boolean run(PassContext ctx);
}
