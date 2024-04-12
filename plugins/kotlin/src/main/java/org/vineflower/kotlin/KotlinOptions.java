package org.vineflower.kotlin;

import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.*;

public interface KotlinOptions {
  @Name("Show public visibility")
  @Description("If a construct is public, show the public keyword")
  @Type(Type.BOOLEAN)
  String SHOW_PUBLIC_VISIBILITY = "kt-show-public";

  @Name("Enable Kotlin plugin")
  @Description("Decompile Kotlin classes as Kotlin instead of Java")
  @Type(Type.BOOLEAN)
  String DECOMPILE_KOTLIN = "kt-enable";

  @Name("Unknown default arg string")
  @Description("String to use for unknown default arguments, or empty to not indicate unknown defaults")
  @Type(Type.STRING)
  String UNKNOWN_DEFAULT_ARG_STRING = "kt-unknown-defaults";

  static void addDefaults(PluginOptions.AddDefaults cons) {
    cons.addDefault(SHOW_PUBLIC_VISIBILITY, "1");
    cons.addDefault(DECOMPILE_KOTLIN, "1");
    cons.addDefault(UNKNOWN_DEFAULT_ARG_STRING, "...");
  }
}
