package org.vineflower.kotlin;

import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.Arrays;

public class KotlinImportCollector {
  private static final String[] AUTO_KOTLIN_IMPORTS = {
    "annotation",
    "collections",
    "comparisons",
    "io",
    "jvm",
    "ranges",
    "sequences",
    "text",
  };

  private final ImportCollector parent;

  public KotlinImportCollector(ImportCollector parent) {
    this.parent = parent;
  }

  public void writeImports(TextBuffer buffer, boolean addSeparator) {
    TextBuffer buf = new TextBuffer();
    parent.writeImports(buf, false);
    String[] imports = buf.convertToStringAndAllowDataDiscard().split("\n");
    boolean imported = false;
    for (String line : imports) {
      if (line.isBlank()) {
        continue;
      }
      line = line.trim();
      String importLine = line.substring(7, line.length() - 1);
      String[] parts = importLine.split("\\.");

      // Don't include automatic kotlin imports
      if (parts.length == 2 && parts[0].equals("kotlin")) {
        continue;
      } else if (parts.length == 3 && parts[0].equals("kotlin") && Arrays.binarySearch(AUTO_KOTLIN_IMPORTS, parts[1]) >= 0) {
        continue;
      }

      buffer.append("import ");
      boolean first = true;
      for (String part : parts) {
        if (!first) {
          buffer.append(".");
        }
        first = false;
        buffer.append(KotlinWriter.toValidKotlinIdentifier(part));
      }
      buffer.appendLineSeparator();
      imported = true;
    }
    if (imported && addSeparator) {
      buffer.appendLineSeparator();
    }
  }
}
