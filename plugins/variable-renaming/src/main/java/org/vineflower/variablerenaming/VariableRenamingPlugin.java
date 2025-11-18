package org.vineflower.variablerenaming;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IVariableNamingFactory;
import org.jetbrains.java.decompiler.util.Pair;

public class VariableRenamingPlugin implements Plugin {
  @Override
  public String id() {
    return "VariableRenaming";
  }

  @Override
  public String description() {
    return "Allows automatic renaming of variables with a common naming scheme.";
  }

  @Override
  public @Nullable PluginOptions getPluginOptions() {
    return () -> Pair.of(VariableRenamingOptions.class, VariableRenamingOptions::addDefaults);
  }

  @Override
  public @Nullable IVariableNamingFactory getRenamingFactory() {
    String name = (String) DecompilerContext.getProperty(VariableRenamingOptions.VARIABLE_RENAMER);
    if (name != null) {
      return Renamers.get(name);
    }

    // Honor legacy option
    if (DecompilerContext.getOption(VariableRenamingOptions.USE_JAD_VARNAMING)) {
      return Renamers.get("jad");
    }

    return null;
  }

  static {
    Renamers.registerProvider("jad", new JADNameProvider.JADNameProviderFactory());
    Renamers.registerProvider("tiny", new TinyNameProvider.TinyNameProviderFactory());
  }
}
