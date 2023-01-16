package org.jetbrains.java.decompiler.main.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginSources {
  public static List<PluginSource> PLUGIN_SOURCES = new ArrayList<>();

  static {
    PLUGIN_SOURCES.add(new JarPluginSource());
    PLUGIN_SOURCES.add(new ServicePluginSource());
  }
}
