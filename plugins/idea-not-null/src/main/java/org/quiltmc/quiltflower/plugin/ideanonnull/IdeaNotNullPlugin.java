package org.quiltmc.quiltflower.plugin.ideanonnull;

import org.jetbrains.java.decompiler.api.Plugin;
import org.jetbrains.java.decompiler.api.java.JavaPassLocation;
import org.jetbrains.java.decompiler.api.java.JavaPassRegistrar;
import org.jetbrains.java.decompiler.api.passes.NamedPass;
import org.jetbrains.java.decompiler.api.passes.Pass;

import java.util.List;

public class IdeaNotNullPlugin implements Plugin {
  
  public String id() {
    return "IdeaNotNull";
  }

  @Override
  public void registerJavaPasses(JavaPassRegistrar registrar) {
    registrar.register(JavaPassLocation.MAIN_LOOP, new NamedPass("IdeaNotNull", new IdeaNotNullPass()));
  }
}
