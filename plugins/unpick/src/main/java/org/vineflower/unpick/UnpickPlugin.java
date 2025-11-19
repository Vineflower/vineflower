package org.vineflower.unpick;

import org.jetbrains.java.decompiler.api.java.JavaPassLocation;
import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.api.plugin.pass.NamedPass;
import org.jetbrains.java.decompiler.util.Pair;

public class UnpickPlugin implements Plugin {
  @Override
  public String id() {
    return "Unpick";
  }

  @Override
  public String description() {
    return "Uninlines constants based on unpick definition files";
  }

  @Override
  public void registerJavaPasses(JavaPassRegistrar registrar) {
    registrar.register(JavaPassLocation.AT_END, NamedPass.of("unpick", new UnpickPass()));
  }

  @Override
  public PluginOptions getPluginOptions() {
    return () -> Pair.of(UnpickOptions.class, UnpickOptions::addDefaults);
  }
}
