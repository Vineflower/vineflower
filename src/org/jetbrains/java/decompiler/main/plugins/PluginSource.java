package org.jetbrains.java.decompiler.main.plugins;

import org.jetbrains.java.decompiler.api.Plugin;

import java.util.List;

public interface PluginSource {
  List<Plugin> findPlugins();
}
