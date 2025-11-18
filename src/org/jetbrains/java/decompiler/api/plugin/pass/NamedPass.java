package org.jetbrains.java.decompiler.api.plugin.pass;

public final class NamedPass implements Pass {
  private final String name;
  private final Pass pass;

  public NamedPass(String name, Pass pass) {
    this.name = name;
    this.pass = pass;
  }

  public static NamedPass of(String name, Pass pass) {
    return new NamedPass(name, pass);
  }

  @Override
  public boolean run(PassContext ctx) {
    boolean res = this.pass.run(ctx);

    if (res) {
      ctx.getRec().add(this.name, ctx.getRoot());
    }

    return res;
  }
}
