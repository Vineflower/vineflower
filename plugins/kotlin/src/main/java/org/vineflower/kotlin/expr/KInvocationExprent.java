package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;

public class KInvocationExprent extends InvocationExprent implements KExprent {
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

  @Override
  public Exprent copy() {
    return new KInvocationExprent((InvocationExprent) super.copy());
  }
}
