package org.vineflower.variablerenaming;

import org.jetbrains.java.decompiler.main.extern.IVariableNamingFactory;

import java.util.HashMap;
import java.util.Map;

public class Renamers {
  private static final Map<String, IVariableNamingFactory> PROVIDERS = new HashMap<>();

  public static void registerProvider(String name, IVariableNamingFactory factory) {
    PROVIDERS.put(name, factory);
  }

  public static IVariableNamingFactory get(String name) {
    return PROVIDERS.get(name);
  }
}
