package org.jetbrains.java.decompiler.main.plugins;

import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.plugin.PluginSource;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ServicePluginSource implements PluginSource {
    public List<Plugin> findPlugins() {
      List<Plugin> plugins = new ArrayList<>();
      for (Plugin plugin : ServiceLoader.load(Plugin.class, getClass().getClassLoader())) {
        plugins.add(plugin);
      }

      return plugins;
    }
}
