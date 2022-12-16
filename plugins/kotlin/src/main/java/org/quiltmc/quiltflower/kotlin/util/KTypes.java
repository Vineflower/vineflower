package org.quiltmc.quiltflower.kotlin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KTypes {
  private static final Pattern GENERICS_PATTERN = Pattern.compile("(.+)<(.+)>");
  // TODO: check clashes with java.lang.* types
  public static String mapJavaTypeToKotlin(String type) {
    if (type.endsWith("[]")) {
      String baseType = type.substring(0, type.length() - 2);
      switch (baseType) {
        case "boolean":
        case "byte":
        case "char":
        case "short":
        case "int":
        case "long":
        case "float":
        case "double":
          return mapJavaTypeToKotlin(baseType) + "Array";
        default:
          return "Array<" + mapJavaTypeToKotlin(baseType) + ">";
      }
    }
    
    switch (type) {
      case "boolean":
        return "Boolean";
      case "byte":
        return "Byte";
      case "char":
        return "Char";
      case "short":
        return "Short";
      case "int":
        return "Int";
      case "long":
        return "Long";
      case "float":
        return "Float";
      case "double":
        return "Double";
      case "void":
        return "Unit";

      default:
        String[] parts = type.split("\\.");
        StringBuilder sb = new StringBuilder();
        boolean appendDot = false;
        for (String part : parts) {
          Matcher matcher = GENERICS_PATTERN.matcher(part);
          if (matcher.matches()) {
            sb.append(mapJavaTypeToKotlin(matcher.group(1)));
            sb.append("<");
            boolean appendComma = false;
            for (String generic : matcher.group(2).split(",")) {
              if (appendComma) {
                sb.append(", ");
              }
              sb.append(mapJavaTypeToKotlin(generic.trim()));
              appendComma = true;
            }
            sb.append(">");
          } else {
            if (appendDot) {
              sb.append(".");
            }
            sb.append(part);
            appendDot = true;
          }
        }
        return sb.toString();
    }
  }
}
