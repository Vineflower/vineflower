package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsoleHelp {
  private static final String[] DEFAULT_HELP = {
    "Usage: java -jar quiltflower.jar [--<option>=<value>]* [<source>]+ <destination>",
    "At least one source file or directory must be specified.",
    "Options:",
    "-h, --help: Show this help",
    "", "Saving options",
    "A maximum of one of the options can be specified:",
    "--file          - Write the decompiled source to a file",
    "--folder        - Write the decompiled source to a folder",
    "--legacy-saving - Use the legacy console-specific method of saving",
    "If unspecified, the decompiled source will be automatically detected based on destination name.",
    "", "General options",
    "These options can be specified multiple times.",
    "-e=<path>, --add-external=<path> - Add the specified path to the list of external libraries",
    "-only=<class>, --only=<class>    - Only decompile the specified class",
    "", "Additional options",
    "These options take the last specified value.",
    "They are mostly specified with a name followed by an equals sign, followed by the value.",
    "Boolean options can also be specified without a value, in which case they are treated as `true`.",
    "They can also be specified with a `no-` prefix, in which case they are treated as `false`.",
    "For example, `--decompile-generics` is equivalent to `--decompile-generics=true`.",
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
        .append("=<")
        .append(type.value())
        .append("> - ")
        .append(name.value())
        .append(": ")
        .append(description.value());

      if (defaults.containsKey(paramName)) {
        sb.append(" (default: ");
        Object value = defaults.get(paramName);
        if (type.value().equals(IFernflowerPreferences.Type.BOOLEAN)) {
          sb.append(value.equals("1"));
        } else {
          sb.append(value);
        }
        sb.append(")");
      }

      System.out.println(sb);
    }
  }
}
