package org.jetbrains.java.decompiler.api.plugin.pass;

import java.util.function.Consumer;

public final class WrappedPass implements Pass {
  private final Consumer<PassContext> pass;

  private WrappedPass(Consumer<PassContext> pass) {
    this.pass = pass;
  }

  public static Pass of(Consumer<PassContext> pass) {
    return new WrappedPass(pass);
  }

  @Override
  public boolean run(PassContext ctx) {
    this.pass.accept(ctx);

    return true;
  }
}
