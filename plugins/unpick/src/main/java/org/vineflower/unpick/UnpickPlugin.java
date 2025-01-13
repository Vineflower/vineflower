package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.java.JavaPassLocation;
import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.api.plugin.pass.NamedPass;

public class UnpickPlugin implements Plugin {
  @Override
  public String id() {
    return "Unpick";
  }

  @Override
  public @Nullable PluginOptions getPluginOptions() {
    return null;
  }

  @Override
  public void registerJavaPasses(JavaPassRegistrar registrar) {
    registrar.register(JavaPassLocation.AT_END, NamedPass.of("Unpick", new UnpickPass()));
  }

  @Override
  public String description() {
    return "Allows constant de-inlining in decompiled code";
  }
}
