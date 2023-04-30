package org.jetbrains.java.decompiler.api;

import org.jetbrains.java.decompiler.api.Plugin;

import java.util.List;

public interface PluginSource {
  List<Plugin> findPlugins();
}
