package org.quiltmc.quiltflower.plugin.ideanonnull;

import org.jetbrains.java.decompiler.api.Plugin;
import org.jetbrains.java.decompiler.api.passes.NamedPass;
import org.jetbrains.java.decompiler.api.passes.Pass;

import java.util.List;

public class IdeaNotNullPlugin implements Plugin{
  
  public String id(){
    return "idea-not-null";
  }
  
  public List<Pass> passes(){
    return List.of(new NamedPass("IdeaNotNull", new IdeaNotNullPass()));
  }
}
