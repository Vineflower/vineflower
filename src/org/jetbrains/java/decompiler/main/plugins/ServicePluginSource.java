package org.jetbrains.java.decompiler.main.plugins;

import org.jetbrains.java.decompiler.api.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ServicePluginSource implements PluginSource {
    public List<Plugin> findPlugins() {
      List<Plugin> plugins = new ArrayList<>();
      for (Plugin plugin : ServiceLoader.load(Plugin.class)) {
        plugins.add(plugin);
      }

      return plugins;
    }
}
