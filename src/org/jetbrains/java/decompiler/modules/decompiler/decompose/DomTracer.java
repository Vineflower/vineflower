package org.jetbrains.java.decompiler.modules.decompiler.decompose;

import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.DotExporter;

import java.util.HashMap;
import java.util.Map;

class DomTracer {
  private static final boolean COLLECT_STRINGS = false;
  private static final boolean COLLECT_DOTS = true;

  private final String filePrefix;
  private final StructMethod structMethod;
  private String string = "";
  private int counter = 0;

  DomTracer(String filePrefix, StructMethod structMethod) {
    this.filePrefix = filePrefix;
    this.structMethod = structMethod;
  }

  private void add(Statement gen, String s, Map<Statement, String> props) {
    if (COLLECT_STRINGS) {
      this.string += ("[" + gen + "] " + s + "\n");
    }

    if (COLLECT_DOTS) {
      HashMap<Statement, String> map = new HashMap<>(props);
      if (map.containsKey(gen)) {
        map.put(gen, map.get(gen) + ",xlabel=\"" + s + "\"");
      } else {
        map.put(gen, "xlabel=\"" + s + "\"");
      }
      DotExporter.toDotFile(gen, this.structMethod, this.filePrefix, "g" + this.counter, map);
    }

    this.counter++;
  }

  void error(Statement stat, String s) {
    this.add(stat, s, Map.of(stat, "fillcolor=coral1,style=filled"));
  }

  void warn(Statement stat, String s) {
    this.add(stat, s, Map.of(stat, "fillcolor=tan1,style=filled"));
  }

  void info(Statement stat, String s) {
    this.add(stat, s, Map.of(stat, "fillcolor=lightblue,style=filled"));
  }

  void success(Statement stat, String s) {
    this.add(stat, s, Map.of(stat, "fillcolor=lightgreen,style=filled"));
  }

  void success(Statement stat, String s, Statement stat2) {
    this.add(stat, s, Map.of(stat, "fillcolor=lightgreen,style=filled", stat2, "fillcolor=lawngreen,style=filled"));
  }

  @Override
  public String toString() {
    return this.string;
  }
}
