package org.quiltmc.quiltflower.kotlin.util;

public final class KTypes {
  // TODO: check clashes with java.lang.* types
  public static String mapJavaTypeToKotlin(String type) {
    if (type.endsWith("[]")) {
      return "Array<" + mapJavaTypeToKotlin(type.substring(0, type.length() - 2)) + ">";
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
      default:
        return type;
    }
  }
}
