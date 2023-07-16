package org.vineflower.kotlin.util;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KTypes {
  private static final Pattern GENERICS_PATTERN = Pattern.compile("(.+)<(.+)>");
  private static final Map<String, String> JAVA_CLASS_TRANSLATIONS = Map.of(
    "java/lang/Integer", "Int",
    "java/lang/Object", "Any"
  );
  
  private static final Map<String, String> JAVA_GENERIC_TRANSLATIONS = Map.of(
    "java/util/Map", "MutableMap",
    "java/util/HashMap", "HashMap",
    "java/util/List", "MutableList",
    "java/util/ArrayList", "ArrayList",
    "java/util/Set", "MutableSet",
    "java/util/HashSet", "HashSet",
    "java/util/Collection", "MutableCollection",
    "java/util/Iterator", "MutableIterator",
    "java/util/Map$Entry", "MutableMap.MutableEntry",
    "java/util/Iterable", "MutableIterable"
  );

  public static String getKotlinType(VarType type) {
    return getKotlinType(type, true);
  }

  public static String getKotlinType(VarType type, boolean includeOuterClasses) {
    String typeStr;
    if (isFunctionType(type)) {
      typeStr = functionTypeToKotlin(type);
    } else if (JAVA_CLASS_TRANSLATIONS.containsKey(type.value)) {
      typeStr = JAVA_CLASS_TRANSLATIONS.get(type.value);
    } else if (JAVA_GENERIC_TRANSLATIONS.containsKey(type.value) && type.isGeneric()) {
      GenericType genericType = ((GenericType) type);
      List<VarType> arguments = genericType.getArguments();
      String genericName = JAVA_GENERIC_TRANSLATIONS.get(type.value);
      if (arguments.isEmpty()) {
        typeStr = genericName;
      } else {
        StringBuilder builder = new StringBuilder(genericName);
        builder.append("<");
        for (int i = 0; i < arguments.size(); i++) {
          if (i != 0) {
            builder.append(", ");
          }
          builder.append(getKotlinType(arguments.get(i)));
        }
        builder.append(">");
        typeStr = builder.toString();
      }
    } else {
      String s = ExprProcessor.getCastTypeName(type);
      if (ExprProcessor.UNDEFINED_TYPE_STRING.equals(s)) {
        s = "Any";
      }
      // When going this path, arrays already get captured so no need to fall through to the array handling
      if (!includeOuterClasses) {
        s = s.substring(s.lastIndexOf('.') + 1);
      }

      return mapJavaTypeToKotlin(s);
    }
    return "Array<".repeat(type.arrayDim) + typeStr + ">".repeat(type.arrayDim);
  }

  private static String mapJavaTypeToKotlin(String type) {
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

  public static boolean isFunctionType(VarType type) {
    return type.value.startsWith("kotlin/jvm/functions/Function");
  }

  private static String functionTypeToKotlin(VarType type) {
    if (!isFunctionType(type)) {
      throw new IllegalArgumentException("Not a function type: " + type);
    }

    String paramCount = type.value.substring("kotlin/jvm/functions/Function".length());
    if (paramCount.equals("N") || !(type instanceof GenericType)) {
      // TODO: support FunctionN properly
      String typeStr = ExprProcessor.getCastTypeName(type);
      if (ExprProcessor.UNDEFINED_TYPE_STRING.equals(typeStr)) {
        return "Any";
      }
      return mapJavaTypeToKotlin(typeStr);
    }
    
    GenericType genericType = (GenericType) type;
    List<VarType> params = genericType.getArguments();

    int paramCountInt = Integer.parseInt(paramCount);
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (int i = 0; i < paramCountInt; i++) {
      if (i > 0) {
        sb.append(", ");
      }

      VarType param = params.get(i);
      String paramStr = getKotlinType(param);
      if (isFunctionType(param)) {
        paramStr = "(" + paramStr + ")";
      }
      sb.append(paramStr);
      // TODO: check for nullability in types
      sb.append("?");
    }

    sb.append(") -> ");
    VarType returnType = params.get(paramCountInt);
    String returnStr = getKotlinType(returnType);
    if (isFunctionType(returnType)) {
      returnStr = "(" + returnStr + ")";
    }
    sb.append(returnStr);

    return sb.toString();
  }
}
