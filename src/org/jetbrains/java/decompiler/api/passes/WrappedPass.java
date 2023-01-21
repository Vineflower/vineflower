package org.jetbrains.java.decompiler.api.passes;

import java.util.function.Consumer;

public class WrappedPass implements Pass {
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
