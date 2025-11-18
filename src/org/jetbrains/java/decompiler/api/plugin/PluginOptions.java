package org.jetbrains.java.decompiler.api.plugin;

import org.jetbrains.java.decompiler.util.Pair;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface PluginOptions {
  Pair<Class<?>, Consumer<AddDefaults>> provideOptions();

  @FunctionalInterface
  interface AddDefaults {
    void addDefault(String key, Object defaultVal);
  }
}
