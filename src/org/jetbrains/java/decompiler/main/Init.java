package org.jetbrains.java.decompiler.main;

import org.jetbrains.java.decompiler.main.plugins.JarPluginLoader;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;

public final class Init {
  private static boolean initialized = false;
  public static void init() {
      if (initialized) {
        return;
      }

      initialized = true;

    // Load all Java code attributes
    StructGeneralAttribute.init();

    // Class-load all plugins that potentially could be included in the jar
    JarPluginLoader.init();
  }
}
