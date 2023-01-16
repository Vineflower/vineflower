package org.jetbrains.java.decompiler.api;

import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.api.language.LanguageSpec;
import org.jetbrains.java.decompiler.api.passes.Pass;

import java.util.List;

public interface Plugin {
  
  String id();
  
  default void registerJavaPasses(JavaPassRegistrar registrar) {

  }

  default LanguageSpec getLanguageSpec() {
    return null;
  }
}