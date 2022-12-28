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
    "Booleans are traditionally indicated with `0` or `1`, but may also be specified with `true` or `false`.",
    "Because of this, an option shown in this list as a boolean may actually be a number.",
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

      if (name == null || description == null) {
        continue;
      }

      StringBuilder sb = new StringBuilder();
      sb.append(isShortName ? "-" : "--")
        .append(paramName)
        .append("=<");

      String type;
      String defaultValue = (String) defaults.get(paramName);
      if (defaultValue == null) {
        sb.append("string>");
        type = null;
      } else if (defaultValue.equals("0") || defaultValue.equals("1")) {
        sb.append("bool>");
        type = "bool";
      } else {
        try {
          Integer.parseInt(defaultValue);
          sb.append("int>");
          type = "int";
        } catch (NumberFormatException e) {
          sb.append("string>");
          type = "string";
        }
      }

      sb.append(" - ")
        .append(name.value())
        .append(": ")
        .append(description.value());

      if (type != null) {
        sb.append(" (default: ");
        switch (type) {
          case "bool":
            sb.append(defaultValue.equals("1"));
            break;
          case "int":
            sb.append(defaultValue);
            break;
          case "string":
            sb.append('"').append(defaultValue).append('"');
            break;
        }
        sb.append(")");
      }

      System.out.println(sb);
    }
  }
}
