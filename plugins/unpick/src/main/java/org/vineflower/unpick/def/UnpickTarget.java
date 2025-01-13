package org.vineflower.unpick.def;

import org.jetbrains.java.decompiler.struct.gen.VarType;

public class UnpickTarget {
  private final String name;
  private final String value;

  public UnpickTarget(String name, String value, VarType kind, String destination) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
