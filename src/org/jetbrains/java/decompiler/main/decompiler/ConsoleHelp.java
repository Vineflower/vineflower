package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.main.Init;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.plugins.PluginContext;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ConsoleHelp {
  private static final String[] DEFAULT_HELP = {
    "=== Vineflower Decompiler " + ConsoleDecompiler.version() + " ===",
    "",
    "--- Command-line options ---",
    "-h, --help: Print this help screen",
    "-s, --silent: Only print to the console if an error is raised",
    "--list-plugins: Displays loaded plugin information",
    "",
    "--- Decompiler usage ---",
    "Usage: java -jar vineflower.jar --<option>=<value>... <source>... <destination>",
    "At least one source file or directory must be specified.",
    "",
    "--- Saving options ---",
    "A maximum of one of the options can be specified:",
    "--file          - Write the decompiled source to a file",
    "--folder        - Write the decompiled source to a folder",
    "--legacy-saving - Use the legacy console-specific method of saving",
    "If unspecified, the decompiled source will be automatically detected based on destination name.",
    "",
    "--- General options ---",
    "These options can be specified multiple times.",
    "-e=<path>, --add-external=<path> - Add the specified path to the list of external libraries",
    "-only=<class>, --only=<class>    - Only decompile the specified class",
    "",
    "--- Additional options ---",
    "These options take the last specified value.",
    "They are mostly specified with a name followed by an equals sign, followed by the value.",
    "Boolean options can also be specified without a value, in which case they are treated as `true`.",
    "They can also be specified with a `no-` prefix, in which case they are treated as `false`.",
    "For example, `--decompile-generics` is equivalent to `--decompile-generics=true`.",
    "",
    "====== Options from the core decompiler ======"
    // Options are added at runtime
  };

  static void printHelp() {
    for (String line : DEFAULT_HELP) {
      System.out.println(line);
    }

    List<Field> fields = Arrays.stream(IFernflowerPreferences.class.getDeclaredFields())
      .filter(field -> field.getType() == String.class)
      .collect(Collectors.toList());

    Map<String, Object> defaults = IFernflowerPreferences.DEFAULTS;

    writeOptions(fields, defaults);

    PluginContext ctx = new PluginContext();

    ctx.findPlugins();

    for (Plugin plugin : ctx.getPlugins()) {
      PluginOptions opt = plugin.getPluginOptions();

      if (opt != null) {
        var opts = opt.provideOptions();

        List<Field> pluginFields = Arrays.stream(opts.a.getDeclaredFields())
          .filter(field -> field.getType() == String.class)
          .collect(Collectors.toList());

        Map<String, Object> pluginDefaults = new HashMap<>();
        opts.b.accept(pluginDefaults::put);

        if (!pluginFields.isEmpty()) {
          System.out.println();
          System.out.println("====== Options for plugin '" + plugin.id() + "' ======");
          writeOptions(pluginFields, pluginDefaults);
        }
      }
    }
  }

  private static void writeOptions(List<Field> fields, Map<String, Object> defaults) {
    fields.sort(Comparator.comparing((Field a) -> {
      try {
        return a.get(null).toString();
      } catch (IllegalAccessException e) {
        return "";
      }
    }));

    for (Field field : fields) {
      IFernflowerPreferences.Name name = field.getAnnotation(IFernflowerPreferences.Name.class);
      IFernflowerPreferences.Description description = field.getAnnotation(IFernflowerPreferences.Description.class);
      IFernflowerPreferences.Type type = field.getAnnotation(IFernflowerPreferences.Type.class);

      String paramName;
      boolean isShortName = false;
      try {
        paramName = (String) field.get(null);
      } catch (IllegalAccessException e) {
        IFernflowerPreferences.ShortName shortName = field.getAnnotation(IFernflowerPreferences.ShortName.class);
        if (shortName == null) {
          continue;
        }
        paramName = shortName.value();
        isShortName = true;
      }

      if (name == null || description == null || type == null) {
        continue;
      }

      StringBuilder sb = new StringBuilder();
      sb.append(isShortName ? "-" : "--")
        .append(paramName)
        .append(" ".repeat(Math.max(40 - paramName.length(), 0)))
        .append("[")
        .append(type.value())
        .append("]")
        .append(" ".repeat(Math.max(8 - type.value().length(), 0)))
        .append(name.value())
        .append(" ".repeat(Math.max(50 - name.value().length(), 0)))
        .append(":");

      StringBuilder sb2 = new StringBuilder();
      if (defaults.containsKey(paramName)) {
        sb2.append(" (default: ");
        Object value = defaults.get(paramName);
        switch (type.value()) {
          case IFernflowerPreferences.Type.BOOLEAN:
            sb2.append(value.equals("1"));
            break;
          case IFernflowerPreferences.Type.STRING:
            sb2.append('"').append(value).append('"');
            break;
          default:
            sb2.append(value);
            break;
        }
        sb2.append(")");
        sb2.append(" ".repeat(Math.max(18 - sb2.length(), 1)));
        sb.append(sb2);
      }

      sb.append(description.value());

      System.out.println(sb);
    }
  }

  static void printPlugins() {
    System.out.println("=== Vineflower Decompiler " + ConsoleDecompiler.version() + " ===");
    System.out.println();

    PluginContext ctx = new PluginContext();
    ctx.findPlugins();

    System.out.println("Loaded " + ctx.getPlugins().size() + " plugins:");
    for (Plugin plugin : ctx.getPlugins()) {
      System.out.println(plugin.id() + " (loaded from " + ctx.getSource(plugin).getClass().getSimpleName() + ")");
      System.out.println(" - " + plugin.description());
      System.out.println();
    }
  }

   static {
     Init.init();
   }
}
