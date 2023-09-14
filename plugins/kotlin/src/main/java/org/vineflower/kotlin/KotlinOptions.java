package org.vineflower.kotlin;

import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.*;

import java.util.Map;

public interface KotlinOptions {
  @Name("Show public visibility")
  @Description("If a construct is public, show the public keyword")
  @Type(Type.BOOLEAN)
  String SHOW_PUBLIC_VISIBILITY = "kt-show-public";

  @Name("Decompile Kotlin")
  @Description("Decompile Kotlin classes as Kotlin instead of Java")
  @Type(Type.BOOLEAN)
  String DECOMPILE_KOTLIN = "kt-decompile-kotlin";

  static void addDefaults(PluginOptions.AddDefaults cons) {
    cons.addDefault(SHOW_PUBLIC_VISIBILITY, "1");
    cons.addDefault(DECOMPILE_KOTLIN, "1");
  }
}
