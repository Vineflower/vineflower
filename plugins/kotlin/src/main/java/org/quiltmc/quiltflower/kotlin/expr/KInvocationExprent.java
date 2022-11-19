package org.quiltmc.quiltflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;

public class KInvocationExprent extends InvocationExprent {
  private boolean shadowStaticBase = false;

  public KInvocationExprent(InvocationExprent expr) {
    super(expr);
  }

  public boolean isShadowStaticBase() {
    return shadowStaticBase;
  }

  public void setShadowStaticBase(boolean shadowStaticBase) {
    this.shadowStaticBase = shadowStaticBase;
  }
}
