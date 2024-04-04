package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class OptionParser {
  private static final Map<String, String> SHORT_TO_LONG_NAME_MAP = new HashMap<>();

  private static final String[] CUSTOM_CHECKS = {
    "-e=",
    "--add-external=",
    "-only=",
    "--only=",
    "-s",
    "--silent"
  };

  public static boolean parse(String arg, Map<String, Object> options) {
    for (String check : CUSTOM_CHECKS) {
      if (arg.startsWith(check)) {
        return false;
      }
    }

    if (!arg.startsWith("--")) {
      parseShort(arg, options);
      return true;
    }

    arg = arg.substring(2);
    int index = arg.indexOf('=');
    if (index == -1) {
      if (arg.startsWith("no-")) {
        options.put(arg.substring(3), "0");
      } else {
        options.put(arg, "1");
      }
    } else {
      String key = arg.substring(0, index);
      String value = arg.substring(index + 1);
      if ("true".equalsIgnoreCase(value)) {
        value = "1";
      } else if ("false".equalsIgnoreCase(value)) {
        value = "0";
      }

      options.put(key, value);
    }
    return true;
  }

  public static void parseShort(String arg, Map<String, Object> options) {
    if (SHORT_TO_LONG_NAME_MAP.isEmpty()) {
      mapShortToLongNames();
    }

    String shortName = arg.substring(1, 4);
    String value = arg.substring(5);
    String longName = SHORT_TO_LONG_NAME_MAP.get(shortName);
    if (longName == null) {
      longName = shortName;
    }
    parse("--" + longName + "=" + value, options);
  }

  private static void mapShortToLongNames() {
    for (Field field : IFernflowerPreferences.class.getDeclaredFields()) {
      IFernflowerPreferences.ShortName shortName = field.getAnnotation(IFernflowerPreferences.ShortName.class);
      if (shortName != null) {
        try {
          SHORT_TO_LONG_NAME_MAP.put(shortName.value(), (String) field.get(null));
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }

    // Hardcode old JAD varnaming from the plugin
    SHORT_TO_LONG_NAME_MAP.put("jvn", "jad-style-variable-naming");
    SHORT_TO_LONG_NAME_MAP.put("jpr", "jad-style-parameter-naming");
  }
}
