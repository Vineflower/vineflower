package org.jetbrains.java.decompiler.modules.decompiler.flow;

import java.util.Locale;

public enum DirectNodeType {
  DIRECT() {
    @Override
    protected String makeId(int statId) {
      return "" + statId;
    }
  },
  TAIL,
  INIT, CONDITION, INCREMENT,
  TRY, CATCH, COMBINED_CATCH,
  FINALLY, FINALLY_END,
  FOREACH_VARDEF,
  CASE;

  private final String name;

  DirectNodeType() {
    if (this.name().equals("DIRECT")){
      this.name = "";
    } else{
      this.name = this.name().toLowerCase(Locale.ROOT);
    }
  }

  protected String makeId(int statId) {
    return statId + "_" + this.name;
  }
}
