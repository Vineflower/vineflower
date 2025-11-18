package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.struct.StructContext;

import java.util.HashMap;

/**
 * Sets up enough data for statements to work outside of a decompiler setting.
 */
public final class MinimalFernflowerEnvironment {
  public static void setup() {
    StructContext sc = new StructContext(null, null, null);
    DecompilerContext context = new DecompilerContext(
      new HashMap<>(),
      new PrintStreamLogger(System.out),
      sc,
      new ClassesProcessor(sc),
      null);
    DecompilerContext.setCurrentContext(context);
  }
}
