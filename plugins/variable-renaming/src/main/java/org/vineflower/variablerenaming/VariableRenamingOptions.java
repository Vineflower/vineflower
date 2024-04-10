package org.vineflower.variablerenaming;

import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.*;

public interface VariableRenamingOptions {
  @Name("Variable Renaming")
  @Description("Use a custom renamer for variable names. Built-in options include \"jad\" and \"tiny\".")
  @Type(Type.STRING)
  String VARIABLE_RENAMER = "variable-renaming";

  @Name("Rename Parameters")
  @Description("Use the custom renamer for parameters in addition to locals.")
  @Type(Type.BOOLEAN)
  String RENAME_PARAMETERS = "rename-parameters";

  @Name("[Deprecated] JAD-Style Variable Naming")
  @Description("Use JAD-style variable naming. Deprecated, set \"variable-renamer=jad\" instead.")
  @Type(Type.BOOLEAN)
  String USE_JAD_VARNAMING = "jad-style-variable-naming";

  @Name("[Deprecated] JAD-Style Parameter Naming")
  @Description("Alias for \"rename-parameters\". Deprecated, use that option instead.")
  @Type(Type.BOOLEAN)
  String USE_JAD_PARAMETER_NAMING = "jad-style-parameter-naming";

  static void addDefaults(PluginOptions.AddDefaults cons) {
    cons.addDefault(VARIABLE_RENAMER, null);
    cons.addDefault(RENAME_PARAMETERS, "0");
    cons.addDefault(USE_JAD_VARNAMING, "0");
    cons.addDefault(USE_JAD_PARAMETER_NAMING, "0");
  }
}
