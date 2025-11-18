package org.jetbrains.java.decompiler.main.plugins;

import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.java.JavaPassLocation;
import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.api.plugin.LanguageSpec;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.api.plugin.PluginSource;
import org.jetbrains.java.decompiler.api.plugin.pass.NamedPass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.decompiler.CancelationManager;
import org.jetbrains.java.decompiler.main.extern.IVariableNameProvider;
import org.jetbrains.java.decompiler.main.extern.IVariableNamingFactory;
import org.jetbrains.java.decompiler.struct.StructClass;

import java.util.*;
import java.util.regex.Pattern;

public class PluginContext {
  private final List<Plugin> plugins = new ArrayList<>();
  private final Map<Plugin, PluginSource> bySource = new HashMap<>();
  private boolean initialized = false;
  private Map<JavaPassLocation, List<NamedPass>> passes = new HashMap<>();
  private final Map<Plugin, LanguageSpec> languageSpecs = new HashMap<>();
  private final Set<String> ids = new HashSet<>();

  public static PluginContext getCurrentContext() {
    return DecompilerContext.getCurrentContext().structContext.getPluginContext();
  }

  private void registerPlugin(Plugin plugin, PluginSource source) {
    plugins.add(plugin);
    bySource.put(plugin, source);
  }

  public int findPlugins() {
    int pluginCount = 0;
    for (PluginSource source : PluginSources.PLUGIN_SOURCES) {
      for (Plugin plugin : source.findPlugins()) {
        registerPlugin(plugin, source);
        pluginCount++;
      }
    }

    return pluginCount;
  }

  public void initialize() {
    if (initialized) {
      return;
    }

    initialized = true;

    JavaPassRegistrar registrar = new JavaPassRegistrar();
    for (Plugin plugin : plugins) {
      String id = plugin.id();
      if (!ids.add(id)) {
        throw new IllegalStateException("Duplicate plugin " + plugin.getClass().getName() + " with id " + id);
      }

      plugin.registerJavaPasses(registrar);
      LanguageSpec spec = plugin.getLanguageSpec();
      if (spec != null) {
        languageSpecs.put(plugin, spec);
      }

      Map<String, Object> props = DecompilerContext.getCurrentContext().properties;

      PluginOptions opt = plugin.getPluginOptions();

      if (opt != null) {
        // Add default values
        opt.provideOptions().b.accept(props::putIfAbsent);
      }
    }

    passes = registrar.getPasses();
  }

  // Returns whether any passes were run
  public boolean runPasses(JavaPassLocation location, PassContext ctx) {
    List<NamedPass> passes = this.passes.getOrDefault(location, Collections.emptyList());

    for (NamedPass pass : passes) {
      CancelationManager.checkCanceled();
      if (pass.run(ctx) && location.isLoop()) {
        return true;
      }
    }

    return false;
  }

  public LanguageSpec getLanguageSpec(StructClass cl) {
    for (Plugin plugin : plugins) {
      LanguageSpec spec = languageSpecs.get(plugin);
      if (spec != null && spec.chooser.isLanguage(cl)) {
        return spec;
      }
    }

    return null;
  }

  public IVariableNamingFactory getVariableRenamer() {
    for (Plugin plugin : plugins) {
      IVariableNamingFactory factory = plugin.getRenamingFactory();
      if (factory != null) {
        return factory;
      }
    }

    return null;
  }

  public PluginSource getSource(Plugin plugin) {
    return bySource.get(plugin);
  }

  public List<Plugin> getPlugins() {
    return Collections.unmodifiableList(plugins);
  }
}
