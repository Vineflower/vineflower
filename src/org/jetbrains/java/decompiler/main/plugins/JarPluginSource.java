package org.jetbrains.java.decompiler.main.plugins;

import org.jetbrains.java.decompiler.api.Plugin;
import org.jetbrains.java.decompiler.api.PluginSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class JarPluginSource implements PluginSource {
    public JarPluginSource() {
    }

    public List<Plugin> findPlugins() {
      List<Plugin> plugins = new ArrayList<>();
      for (Class<?> cl : JarPluginLoader.PLUGIN_CLASSES) {
        try {
          plugins.add((Plugin) cl.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }

      return plugins;
    }
}
