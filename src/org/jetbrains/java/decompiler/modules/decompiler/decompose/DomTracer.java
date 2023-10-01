package org.jetbrains.java.decompiler.modules.decompiler.decompose;

import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.DotExporter;

import java.util.HashMap;
import java.util.Map;

class DomTracer {
  private static final boolean COLLECT_STRINGS = false;
  private static final boolean COLLECT_DOTS = false;

  private final String filePrefix;
  private final StructMethod structMethod;
  private String string = "";
  private int counter = 0;

  DomTracer(String filePrefix, StructMethod structMethod) {
    this.filePrefix = filePrefix;
    this.structMethod = structMethod;
  }

  private void add(Statement gen, String s, Map<Statement, String> props) {
    if (COLLECT_DOTS) {
      HashMap<Statement, String> map = new HashMap<>(props);
      if (map.containsKey(gen)) {
        map.put(gen, map.get(gen) + ",xlabel=\"" + s + "\"");
      } else {
        map.put(gen, "xlabel=\"" + s + "\"");
      }
      DotExporter.toDotFile(gen, this.structMethod, this.filePrefix, "g" + this.counter, map);

      if (COLLECT_STRINGS) {
        string += "(g" + this.counter +") ";
      }
    }

    if (COLLECT_STRINGS) {
      this.string += ("[" + gen + "] " + s + "\n");
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
    this.add(stat, s, Map.of(stat, "fillcolor=lawngreen,style=filled"));
  }

  void successCreated(Statement stat, String s, Statement newStat) {
    Map<Statement, String> props = new HashMap<>();
    props.put(stat, "fillcolor=orange,style=filled");
    props.put(newStat, "fillcolor=lawngreen,style=filled");
    for (Statement st : newStat.getStats()) {
      props.put(st, "fillcolor=pink,style=filled");
    }
    this.add(stat, s, props);
  }

  @Override
  public String toString() {
    return this.string;
  }
}
