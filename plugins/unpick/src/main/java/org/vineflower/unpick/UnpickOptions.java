package org.vineflower.unpick;

import org.jetbrains.java.decompiler.api.DecompilerOption;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

public interface UnpickOptions {
  @IFernflowerPreferences.Name("Use Built-in Unpick Files")
  @IFernflowerPreferences.Description("Whether to use the built-in unpick files, which contain definitions for the JDK")
  @IFernflowerPreferences.Type(DecompilerOption.Type.BOOLEAN)
  String USE_BUILTIN_UNPICK_FILES = "use-builtin-unpick-files";

  @IFernflowerPreferences.Name("Unpick Files")
  @IFernflowerPreferences.Description("The unpick file, directory of unpick files, or zip of unpick files, to apply")
  @IFernflowerPreferences.Type(DecompilerOption.Type.STRING)
  String UNPICK_FILES = "unpick-files";

  static void addDefaults(PluginOptions.AddDefaults cons) {
    cons.addDefault(USE_BUILTIN_UNPICK_FILES, "1");
    cons.addDefault(UNPICK_FILES, "");
  }
}
