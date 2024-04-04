package org.vineflower.kotlin;

import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.util.KTypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KotlinImportCollector extends ImportCollector {
  public static final String[] AUTO_KOTLIN_IMPORTS = {
    "annotation",
    "collections",
    "comparisons",
    "io",
    "jvm",
    "ranges",
    "sequences",
    "text",
  };

  public KotlinImportCollector(ImportCollector parent) {
    super(parent);

    // Any class that Kotlin "overrides" requires explicit non-imported references
    for (String className : KTypes.KOTLIN_TO_JAVA_LANG.keySet()) {
      String simpleName = className.substring(className.lastIndexOf('/') + 1);
      String packageName = className.substring(0, className.lastIndexOf('/')).replace('/', '.');
      if (!mapSimpleNames.containsKey(simpleName)) {
        mapSimpleNames.put(simpleName, packageName);
      }
    }

    for (String className : KTypes.KOTLIN_TO_JAVA_UTIL.keySet()) {
      String simpleName = className.substring(className.lastIndexOf('/') + 1);
      String packageName = className.substring(0, className.lastIndexOf('/')).replace('/', '.');
      if (!mapSimpleNames.containsKey(simpleName)) {
        mapSimpleNames.put(simpleName, packageName);
      }
    }
  }

  @Override
  protected boolean keepImport(Map.Entry<String, String> ent) {
    if (!super.keepImport(ent)) return false;
    if (ent.getValue().equals("kotlin")) return false;
    for (String autoImport : AUTO_KOTLIN_IMPORTS) {
      if (ent.getValue().equals("kotlin." + autoImport)) return false;
    }
    return true;
  }

  @Override
  public void writeImports(TextBuffer buffer, boolean addSeparator) {
    if (DecompilerContext.getOption(IFernflowerPreferences.REMOVE_IMPORTS)) {
      return;
    }

    List<String> imports = packImports();
    for (String imp : imports) {
      buffer.append("import ").append(imp).appendLineSeparator();
    }
    if (addSeparator && !imports.isEmpty()) {
      buffer.appendLineSeparator();
    }
  }
}
