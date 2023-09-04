package org.jetbrains.java.decompiler.api.plugin;

import java.util.List;

public interface PluginSource {
  List<Plugin> findPlugins();
}
