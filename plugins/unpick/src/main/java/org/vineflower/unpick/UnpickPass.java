package org.vineflower.unpick;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;

public class UnpickPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    return false;
  }
}
