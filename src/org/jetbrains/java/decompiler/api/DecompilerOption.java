package org.jetbrains.java.decompiler.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.plugins.PluginContext;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Represents a decompiler option. These can be passed from command line or as
 * a map into a {@link BaseDecompiler} or {@link Fernflower} constructor.
 * <p>
 * As plugins might not provide all information, some fields are nullable.
 *
 * @param id The unique identifier of the option. This is what is passed to the decompiler.
 * @param name A human-readable name for the option.
 * @param description A human-readable description of the option.
 * @param type The type of the option.
 * @param plugin The plugin that provides this option. If {@code null}, it comes from the core decompiler.
 * @param defaultValue The default value of the option.
 */
public record DecompilerOption(
  @NotNull String id,
  @NotNull String name,
  @NotNull String description,
  @NotNull Type type,
  @Nullable String plugin,
  @Nullable String defaultValue
) implements Comparable<DecompilerOption> {
  public enum Type {
    BOOLEAN("bool"),
    STRING("string"),
    INTEGER("int"),

    ;

    private final String friendlyName;

    Type(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String toString() {
      return friendlyName;
    }
  }

  /**
   * Compares this option to another option. The order is set first by the plugin, then by the id.
   * Core decompiler options come first.
   */
  @Override
  public int compareTo(@NotNull DecompilerOption decompilerOption) {
    if (!Objects.equals(decompilerOption.plugin, plugin)) {
      if (plugin == null) {
        return -1;
      }
      if (decompilerOption.plugin == null) {
        return 1;
      }
      return plugin.compareTo(decompilerOption.plugin);
    }
    return id.compareTo(decompilerOption.id);
  }

  /**
   * Get all decompiler options from all plugins and the core decompiler.
   * @return A list of all decompiler options, sorted by plugin and id, with core decompiler options first.
   */
  public static List<DecompilerOption> getAll() {
    List<DecompilerOption> options = new ArrayList<>();

    List<Field> fields = Arrays.stream(IFernflowerPreferences.class.getDeclaredFields())
      .filter(field -> field.getType() == String.class)
      .toList();

    Map<String, Object> defaults = IFernflowerPreferences.DEFAULTS;
    addOptions(fields, options, defaults, null);

    PluginContext ctx = new PluginContext();
    ctx.findPlugins();

    for (Plugin plugin : ctx.getPlugins()) {
      PluginOptions opt = plugin.getPluginOptions();

      if (opt != null) {
        var opts = opt.provideOptions();

        List<Field> pluginFields = Arrays.stream(opts.a.getDeclaredFields())
          .filter(field -> field.getType() == String.class)
          .toList();

        Map<String, Object> pluginDefaults = new HashMap<>();
        opts.b.accept(pluginDefaults::put);

        addOptions(pluginFields, options, pluginDefaults, plugin);
      }
    }

    options.sort(DecompilerOption::compareTo);

    return options;
  }

  /**
   * Get all decompiler options from all plugins and the core decompiler, grouped by plugin.
   * Calling {@link Map#get} with {@code null} will return the core decompiler options.
   * @return A map of plugins to their decompiler options, sorted by id within each plugin.
   */
  public static Map<Plugin, List<DecompilerOption>> getAllByPlugin() {
    Map<Plugin, List<DecompilerOption>> options = new HashMap<>();

    List<Field> fields = Arrays.stream(IFernflowerPreferences.class.getDeclaredFields())
      .filter(field -> field.getType() == String.class)
      .toList();

    Map<String, Object> defaults = IFernflowerPreferences.DEFAULTS;
    List<DecompilerOption> coreOptions = new ArrayList<>();
    addOptions(fields, coreOptions, defaults, null);
    coreOptions.sort(DecompilerOption::compareTo);
    options.put(null, coreOptions);

    PluginContext ctx = new PluginContext();
    ctx.findPlugins();

    for (Plugin plugin : ctx.getPlugins()) {
      PluginOptions opt = plugin.getPluginOptions();

      if (opt != null) {
        var opts = opt.provideOptions();

        List<Field> pluginFields = Arrays.stream(opts.a.getDeclaredFields())
          .filter(field -> field.getType() == String.class)
          .toList();

        Map<String, Object> pluginDefaults = new HashMap<>();
        opts.b.accept(pluginDefaults::put);

        List<DecompilerOption> pluginOptions = new ArrayList<>();
        addOptions(pluginFields, pluginOptions, pluginDefaults, plugin);

        pluginOptions.sort(DecompilerOption::compareTo);

        options.put(plugin, pluginOptions);
      }
    }

    return options;
  }

  private static void addOptions(List<Field> fields, List<DecompilerOption> options, Map<String, Object> defaults, Plugin plugin) {
    for (Field field : fields) {
      IFernflowerPreferences.Name name = field.getAnnotation(IFernflowerPreferences.Name.class);
      IFernflowerPreferences.Description description = field.getAnnotation(IFernflowerPreferences.Description.class);
      IFernflowerPreferences.Type type = field.getAnnotation(IFernflowerPreferences.Type.class);

      String paramName;
      try {
        paramName = (String) field.get(null);
      } catch (IllegalAccessException e) {
        IFernflowerPreferences.ShortName shortName = field.getAnnotation(IFernflowerPreferences.ShortName.class);
        if (shortName == null) {
          continue;
        }
        paramName = shortName.value();
      }

      if (name == null || description == null || type == null) {
        continue;
      }

      String friendlyName = name.value();
      String friendlyDescription = description.value();
      Type friendlyType = type.value();
      Object defaultValue = defaults.get(paramName);
      String defaultValueString = defaultValue != null ? defaultValue.toString() : null;

      options.add(new DecompilerOption(
        paramName,
        friendlyName,
        friendlyDescription,
        friendlyType,
        plugin != null ? plugin.id() : null,
        defaultValueString
      ));
    }
  }
}
