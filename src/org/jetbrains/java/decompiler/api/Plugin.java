package org.jetbrains.java.decompiler.api;

import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.api.language.LanguageSpec;
import org.jetbrains.java.decompiler.api.passes.Pass;

import java.util.List;

/**
 * Plugins allow users to interface with Quiltflower's decompilation process by providing user-defined passes or language specifications.
 */
public interface Plugin {

  /**
   * Unique id of the current plugin. Must be unique across all loaded plugins, otherwise an error will be raised at runtime.
   * @return id of the plugin
   */
  String id();

  /**
   * Allows addition to the list of passes that will be run during Java decompilation.
   *
   * @param registrar The registrar object to register into.
   */
  default void registerJavaPasses(JavaPassRegistrar registrar) {

  }

  /**
   * Allows the plugin to specify a totally custom decompilation process for a language based on the JVM.
   * @return The language spec, if any.
   */
  default LanguageSpec getLanguageSpec() {
    return null;
  }
}