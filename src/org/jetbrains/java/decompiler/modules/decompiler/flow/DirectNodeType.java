package org.jetbrains.java.decompiler.modules.decompiler.flow;

import java.util.Locale;

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
  TRY("try"),
  CATCH("catch"),
  COMBINED_CATCH("combined_catch"),
  FINALLY("finally"),
  FINALLY_END("finally_end"),
  FOREACH_VARDEF("foreach"),
  CASE("case");

  private final String name;

  DirectNodeType(String name) {
    if (this.name().equals("DIRECT")){
      this.name = "";
    } else{
      this.name = this.name().toLowerCase(Locale.ROOT);
    }
  }

  protected String makeId(int statId) {
    return statId + "_" + name;
  }
}
