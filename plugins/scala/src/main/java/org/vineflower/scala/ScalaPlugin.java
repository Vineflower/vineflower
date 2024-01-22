package org.vineflower.scala;

import org.jetbrains.java.decompiler.api.plugin.Plugin;

public class ScalaPlugin implements Plugin {
  
  public String id(){
    return "Scala";
  }

  @Override
  public String description() {
    return "Detects and decompiles Scala class files.";
  }
}
