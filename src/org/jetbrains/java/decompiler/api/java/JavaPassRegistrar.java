package org.jetbrains.java.decompiler.api.java;

import org.jetbrains.java.decompiler.api.plugin.pass.ClassPass;
import org.jetbrains.java.decompiler.api.plugin.pass.NamedPass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JavaPassRegistrar {
  private final Map<JavaPassLocation, List<NamedPass>> passes = new HashMap<>();
  private final Map<ClassPassLocation, List<ClassPass>> classPasses = new HashMap<>();

  /**
   * Registers a pass to be run at the given code location.
   *
   * @param location When to run this pass
   * @param pass The pass object to run
   */
  public void register(JavaPassLocation location, NamedPass pass) {
    passes.computeIfAbsent(location, k -> new ArrayList<>()).add(pass);
  }

  public void registerClassPass(ClassPassLocation location, ClassPass pass) {
    classPasses.computeIfAbsent(location, k -> new ArrayList<>()).add(pass);
  }

  // Deep, immutable copy
  public Map<JavaPassLocation, List<NamedPass>> getPasses() {
    return Map.copyOf(
      passes.keySet()
        .stream().collect(Collectors.toMap(k -> k, v -> List.copyOf(passes.get(v))))
    );
  }

  public Map<ClassPassLocation, List<ClassPass>> getClassPasses() {
    return Map.copyOf(
      classPasses.keySet()
        .stream().collect(Collectors.toMap(k -> k, v -> List.copyOf(classPasses.get(v))))
    );
  }
}
