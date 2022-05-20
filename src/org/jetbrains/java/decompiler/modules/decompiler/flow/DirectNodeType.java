package org.jetbrains.java.decompiler.modules.decompiler.flow;

public enum DirectNodeType {
  DIRECT("") {
    @Override
    protected String makeId(int statId) {
      return "" + statId;
    }
  },
  TAIL("tail"),
  INIT("init"),
  CONDITION("cond"),
  INCREMENT("inc"),
  TRY("try"), // TODO: why is this used?
  FOREACH_VARDEF("foreach");

  private final String name;

  DirectNodeType(String name) {
    this.name = name;
  }

  protected String makeId(int statId) {
    return statId + "_" + name;
  }
}
