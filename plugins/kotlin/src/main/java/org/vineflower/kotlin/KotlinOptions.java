package org.vineflower.kotlin;

import org.jetbrains.java.decompiler.api.DecompilerOption;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.*;

public interface KotlinOptions {
  @Name("Show public visibility")
  @Description("If a construct is public, show the public keyword")
  @Type(DecompilerOption.Type.BOOLEAN)
  String SHOW_PUBLIC_VISIBILITY = "kt-show-public";

  @Name("Enable Kotlin plugin")
  @Description("Decompile Kotlin classes as Kotlin instead of Java")
  @Type(DecompilerOption.Type.BOOLEAN)
  String DECOMPILE_KOTLIN = "kt-enable";

  @Name("Unknown default arg string")
  @Description("String to use for unknown default arguments, or empty to not indicate unknown defaults")
  @Type(DecompilerOption.Type.STRING)
  String UNKNOWN_DEFAULT_ARG_STRING = "kt-unknown-defaults";

  @Name("Collapse string concatenation")
  @Description("Convert string concatenations to Kotlin string templates.")
  @Type(DecompilerOption.Type.BOOLEAN)
  String COLLAPSE_STRING_CONCATENATION = "kt-collapse-string-concat";
  
  @Name("Always export metadata")
  @Description("If Kotlin decompilation is disabled, metadata will not be parsed. If enabled, this will always parse Kotlin metadata for use by other plugins.")
  @Type(DecompilerOption.Type.BOOLEAN)
  String ALWAYS_EXPORT_METADATA = "kt-export-metadata";

  static void addDefaults(PluginOptions.AddDefaults cons) {
    cons.addDefault(SHOW_PUBLIC_VISIBILITY, "1");
    cons.addDefault(DECOMPILE_KOTLIN, "1");
    cons.addDefault(UNKNOWN_DEFAULT_ARG_STRING, "...");
    cons.addDefault(COLLAPSE_STRING_CONCATENATION, "1");
    cons.addDefault(ALWAYS_EXPORT_METADATA, "0");
  }
}
