package org.jetbrains.java.decompiler.struct.gen.generics;

import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericsChecker {
  private final Map<String, List<VarType>> boundsMap;

  public GenericsChecker(List<String> typeVariables, List<List<VarType>> bounds) {
    boundsMap = new HashMap<>(typeVariables.size(), 1);
    for (int i = 0; i < typeVariables.size(); i++) {
      boundsMap.put(typeVariables.get(i), bounds.get(i));
    }
  }

  private GenericsChecker(Map<String, List<VarType>> existingBounds, List<String> typeVariables, List<List<VarType>> bounds) {
    boundsMap = new HashMap<>(existingBounds);
    for (int i = 0; i < typeVariables.size(); i++) {
      boundsMap.put(typeVariables.get(i), bounds.get(i));
    }
  }

  public GenericsChecker copy(List<String> typeVariables, List<List<VarType>> bounds) {
    return new GenericsChecker(boundsMap, typeVariables, bounds);
  }

  public boolean isProperlyBounded(VarType type, VarType bound) {
    if (type.isSuperset(bound)) {
      return true;
    }

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
