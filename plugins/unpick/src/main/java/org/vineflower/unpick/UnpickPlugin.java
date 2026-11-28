package org.vineflower.unpick;

import org.jetbrains.java.decompiler.api.java.ClassPassLocation;
import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
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
    // important that it's after switch processing and before initializer merging
    registrar.registerClassPass(ClassPassLocation.AFTER_LATE_METHOD_PROCESSING, new UnpickPass());
  }

  @Override
  public PluginOptions getPluginOptions() {
    return () -> Pair.of(UnpickOptions.class, UnpickOptions::addDefaults);
  }
}
