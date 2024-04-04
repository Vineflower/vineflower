package org.jetbrains.java.decompiler.api.plugin.pass;

import java.util.ArrayList;
import java.util.List;

public final class MainPassBuilder {
  private final List<Pass> passes = new ArrayList<>();

  public MainPassBuilder addPass(String name, Pass pass) {
    passes.add(new NamedPass(name, pass));
    return this;
  }

  public MainPassBuilder addPass(NamedPass pass) {
    passes.add(pass);
    return this;
  }

  public Pass build() {
    return new CompiledPass(passes);
  }

  private static final class CompiledPass implements Pass {
    private final List<Pass> passes;

    public CompiledPass(List<Pass> passes) {
      this.passes = new ArrayList<>(passes);
    }

    @Override
    public boolean run(PassContext ctx) {
      boolean res = false;
      for (Pass pass : passes) {
        res |= pass.run(ctx);
      }

      return res;
    }
  }
}
