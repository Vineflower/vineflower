package org.jetbrains.java.decompiler.struct.gen.generics;

import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericsChecker {
  private final Map<String, List<VarType>> boundsMap;

  public GenericsChecker() {
    boundsMap = Map.of();
  }

  public GenericsChecker(List<String> typeVariables, List<List<VarType>> bounds) {
    boundsMap = new HashMap<>(typeVariables.size(), 1);
    for (int i = 0; i < typeVariables.size(); i++) {
      boundsMap.put(typeVariables.get(i), bounds.get(i));
    }
  }

  private GenericsChecker(Map<String, List<VarType>> boundsMap) {
    this.boundsMap = boundsMap;
  }

  public GenericsChecker copy(List<String> typeVariables, List<List<VarType>> bounds) {
    HashMap<String, List<VarType>> newBounds = new HashMap<>(boundsMap);
    for (int i = 0; i < typeVariables.size(); i++) {
      newBounds.put(typeVariables.get(i), bounds.get(i));
    }

    return new GenericsChecker(newBounds);
  }

  public GenericsChecker copy(GenericsChecker parent) {
    HashMap<String, List<VarType>> newBoundsMap = new HashMap<>(this.boundsMap);
    for (Map.Entry<String, List<VarType>> entry : parent.boundsMap.entrySet()) {
      if (!newBoundsMap.containsKey(entry.getKey())) {
        newBoundsMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
      }
    }

    return new GenericsChecker(newBoundsMap);
  }

  public boolean isProperlyBounded(VarType type, VarType bound) {
    if (type.isSuperset(bound)) {
      return true;
    }

    // Get base type if array
    bound = bound.resizeArrayDim(0);

    if (type.type == CodeType.GENVAR && type instanceof GenericType genericType) {
      List<VarType> typeBounds = boundsMap.get(genericType.value);
      if (typeBounds != null) {
        for (VarType typeBound : typeBounds) {
          if (isProperlyBounded(typeBound, bound)) {
            return true;
          }
        }
      }
    }

    return false;
  }
}
