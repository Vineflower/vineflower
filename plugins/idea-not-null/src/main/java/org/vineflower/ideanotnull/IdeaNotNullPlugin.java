package org.vineflower.ideanotnull;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.plugin.Plugin;
import org.jetbrains.java.decompiler.api.java.JavaPassLocation;
import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.api.plugin.PluginOptions;
import org.jetbrains.java.decompiler.api.plugin.pass.NamedPass;
import org.jetbrains.java.decompiler.util.Pair;

public class IdeaNotNullPlugin implements Plugin {
  
  public String id() {
    return "IdeaNotNull";
  }

  @Override
  public String description() {
    return "Decompiles code inserted by Intellij IDEA's @NotNull annotation.";
  }

  @Override
  public void registerJavaPasses(JavaPassRegistrar registrar) {
    registrar.register(JavaPassLocation.MAIN_LOOP, new NamedPass("IdeaNotNull", new IdeaNotNullPass()));
  }

  @Override
  public @Nullable PluginOptions getPluginOptions() {
    return () -> Pair.of(IdeaNotNullOptions.class, IdeaNotNullOptions::addDefaults);
  }
}
