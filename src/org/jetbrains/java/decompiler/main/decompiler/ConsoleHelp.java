package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.api.DecompilerOption;
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
    "These options can be specified multiple times. Multiple values can be passed in with commas.",
    "-e=<path>, --add-external=<path> - Add the specified path to the list of external libraries",
    "-only=<pattern>, --only=<pattern>    - Only decompile classes starting with the given pattern",
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

    Map<Plugin, List<DecompilerOption>> options = DecompilerOption.getAllByPlugin();

    List<DecompilerOption> coreOptions = options.remove(null);
    if (coreOptions != null) {
      writeOptions(coreOptions);
    }

    for (Map.Entry<Plugin, List<DecompilerOption>> entry : options.entrySet()) {
      if (entry.getValue().isEmpty()) {
        continue;
      }

      System.out.println();
      System.out.println("====== Options from " + entry.getKey().id() + " ======");
      writeOptions(entry.getValue());
    }
  }

  private static void writeOptions(List<DecompilerOption> options) {
    for (DecompilerOption option : options) {
      StringBuilder sb = new StringBuilder();
      sb.append("--")
        .append(option.id())
        .append(" ".repeat(Math.max(40 - option.id().length(), 0)))
        .append("[")
        .append(option.type())
        .append("]")
        .append(" ".repeat(Math.max(8 - option.type().toString().length(), 0)))
        .append(option.name())
        .append(" ".repeat(Math.max(50 - option.name().length(), 0)))
        .append(": ");

      StringBuilder sb2 = new StringBuilder();
      if (option.defaultValue() != null) {
        sb2.append("(default: ");
        String value = option.defaultValue();
        switch (option.type()) {
          case BOOLEAN -> sb2.append(value.equals("1"));
          case STRING -> sb2.append('"').append(value).append('"');
          case INTEGER -> sb2.append(value);
        }
        sb2.append(")");
        sb2.append(" ".repeat(Math.max(18 - sb2.length(), 1)));
        sb.append(sb2);
      }

      if (option.description() != null) {
        sb.append(option.description());
      }

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
