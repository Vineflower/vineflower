package org.jetbrains.java.decompiler.api.plugin;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.main.extern.IVariableNamingFactory;

/**
 * Plugins allow users to interface with Vineflower's decompilation process by providing user-defined passes or language specifications.
 */
public interface Plugin {

  /**
   * Unique id of the current plugin. Must be unique across all loaded plugins, otherwise an error will be raised at runtime.
   * @return id of the plugin
   */
  String id();

  /**
   * Short (1-2 sentence) long description of the plugin and its function. This will be presented to users when requested via terminal.
   * @return a short description of the plugin
   */
  String description();

  /**
   * Allows addition to the list of passes that will be run during Java decompilation.
   *
   * @param registrar The registrar object to register into
   */
  default void registerJavaPasses(JavaPassRegistrar registrar) {

  }

  /**
   * Allows the plugin to specify a totally custom decompilation process for a language based on the JVM.
   * @return The language spec, if any.
   */
  @Nullable
  default LanguageSpec getLanguageSpec() {
    return null;
  }

  @Nullable
  default PluginOptions getPluginOptions() {
    return null;
  }

  @Nullable
  default IVariableNamingFactory getRenamingFactory() {
    return null;
  }
}