package org.jetbrains.java.decompiler.api;

import org.jetbrains.java.decompiler.api.passes.Pass;

import java.util.List;

public interface Plugin{
  
  String id();
  
  List<Pass> passes();
  
  // ...
}