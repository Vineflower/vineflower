package org.quiltmc.quiltflower.kotlin;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.*;

import java.util.Map;

public interface KotlinPreferences {
  @Name("Show public visibility")
  @Description("If a construct is public, show the public keyword")
  @Type(Type.BOOLEAN)
  String SHOW_PUBLIC_VISIBILITY = "kt-show-public";

  @Name("Force disable")
  @Description("Disable Kotlin decompilation")
  @Type(Type.BOOLEAN)
  String FORCE_DISABLE = "kt-force-disable";

  Map<String, String> DEFAULTS = Map.ofEntries(
    Map.entry(SHOW_PUBLIC_VISIBILITY, "1"),
    Map.entry(FORCE_DISABLE, "0")
  );

  static String getPreference(String key) {
    Object value = DecompilerContext.getProperty(key);
    return value == null ? DEFAULTS.get(key) : (String) value;
  }
}
