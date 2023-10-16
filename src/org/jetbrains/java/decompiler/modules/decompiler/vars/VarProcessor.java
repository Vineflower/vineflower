// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.collectors.VarNamesCollector;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.FinalType;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute.LocalVariable;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.Pair;
import org.jetbrains.java.decompiler.util.StartEndPair;
import org.jetbrains.java.decompiler.util.TextUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class VarProcessor {
  private final VarNamesCollector varNamesCollector = new VarNamesCollector();
  private final StructMethod method;
  private final MethodDescriptor methodDescriptor;
  private Map<VarVersionPair, String> mapVarNames = new HashMap<>();
  private List<VarVersionPair> params = new ArrayList<>();
  private Map<VarVersionPair, LocalVariable> mapVarLVTs = new HashMap<>();
  private @Nullable VarVersionsProcessor varVersions;
  private final Map<VarVersionPair, String> thisVars = new HashMap<>();
  private final Set<VarVersionPair> externalVars = new HashSet<>();
  private final Map<VarVersionPair, String> clashingNames = new HashMap<>();
  private final Map<VarVersionPair, String> inheritedNames = new HashMap<>();
  private final Set<Integer> syntheticSemaphores = new HashSet<>();
  // var -> (method, var in method)
  private final Map<VarVersionPair, Pair<String, VarVersionPair>> varSources = new HashMap<>();
  public boolean nestedProcessed;

  public VarProcessor(StructMethod mt, MethodDescriptor md) {
    method = mt;
    methodDescriptor = md;
  }

  public void setVarVersions(RootStatement root) {
    VarVersionsProcessor oldProcessor = varVersions;
    varVersions = new VarVersionsProcessor(method, methodDescriptor);
    varVersions.setVarVersions(root, oldProcessor);
  }

  public void setVarDefinitions(RootStatement root) {
    mapVarNames = new HashMap<>();
    VarDefinitionHelper varDef = new VarDefinitionHelper(root, method, this);
    varDef.setVarDefinitions();
    this.clashingNames.putAll(varDef.getClashingNames());
  }

  public void setDebugVarNames(RootStatement root, Map<VarVersionPair, String> mapDebugVarNames) {
    if (varVersions == null) {
      return;
    }

    Map<Integer, VarVersionPair> mapOriginalVarIndices = varVersions.getMapOriginalVarIndices();

    List<VarVersionPair> listVars = new ArrayList<>(mapVarNames.keySet());
    listVars.sort(Comparator.comparingInt(o -> o.var));

    Map<String, Integer> mapNames = new HashMap<>();

    for (VarVersionPair pair : listVars) {
      String name = mapVarNames.get(pair);

      boolean lvtName = false;
      VarVersionPair key = mapOriginalVarIndices.get(pair.var);
      if (key != null) {
        String debugName = mapDebugVarNames.get(key);
        if (debugName != null && TextUtil.isValidIdentifier(debugName, method.getBytecodeVersion(), root.mt)) {
          name = debugName;
          lvtName = true;
        }
      }

      Integer counter = mapNames.get(name);
      mapNames.put(name, counter == null ? counter = 0 : ++counter);

      if (counter > 0 && !lvtName) {
        name += String.valueOf(counter);
      }

      mapVarNames.put(pair, name);
    }

    // Re-run name clashing analysis

    rerunClashing(root);
  }

  public void rerunClashing(RootStatement root) {
    VarDefinitionHelper vardef = new VarDefinitionHelper(root, method, this, false);
    vardef.remapClashingNames(root, method);

    for (Entry<VarVersionPair, String> e : vardef.getClashingNames().entrySet()) {
      if (!params.contains(e.getKey())) {
        this.clashingNames.put(e.getKey(), e.getValue());
      }
    }
  }

  public @Nullable Integer getVarOriginalIndex(int index) {
    if (varVersions == null) {
      return null;
    }
    final VarVersionPair pair = varVersions.getMapOriginalVarIndices().get(index);
    return pair == null ? null : pair.var;
  }

  public void refreshVarNames(VarNamesCollector vc) {
    Map<VarVersionPair, String> tempVarNames = new HashMap<>(mapVarNames);
    for (Entry<VarVersionPair, String> ent : tempVarNames.entrySet()) {
      mapVarNames.put(ent.getKey(), vc.getFreeName(ent.getValue()));
    }
  }

  public Set<Integer> getSyntheticSemaphores() {
    return syntheticSemaphores;
  }

  public VarNamesCollector getVarNamesCollector() {
    return varNamesCollector;
  }

  public VarType getVarType(VarVersionPair pair) {
    return varVersions == null ? null : varVersions.getVarType(pair);
  }

  public void markParam(VarVersionPair pair) {
    params.add(pair);
  }

  public List<VarVersionPair> getParams() {
    return params;
  }

  public void setVarType(VarVersionPair pair, VarType type) {
    if (varVersions != null) {
      varVersions.setVarType(pair, type);
    }
  }

  public @Nullable String getVarName(VarVersionPair pair) {
    return mapVarNames == null ? null : mapVarNames.get(pair);
  }

  public @Nullable String getClashingName(VarVersionPair pair) {
    return this.clashingNames.get(pair);
  }

  public void setClashingName(VarVersionPair pair, String name) {
    this.clashingNames.put(pair, name);
  }

  public void setVarName(VarVersionPair pair, String name) {
    mapVarNames.put(pair, name);
  }

  public void setVarSource(VarVersionPair pair, String method, VarVersionPair original) {
    varSources.put(pair, Pair.of(method, original));
  }

  public void setInheritedName(VarVersionPair pair, String name) {
    setVarName(pair, name);

    inheritedNames.put(pair, name);
  }

  public Map<VarVersionPair, String> getInheritedNames() {
    return this.inheritedNames;
  }

  public Set<VarVersionPair> getUsedVarVersions() {
    return mapVarNames != null ? mapVarNames.keySet() : Collections.emptySet();
  }

  public Collection<String> getVarNames() {
    return mapVarNames != null ? mapVarNames.values() : Collections.emptySet();
  }

  public FinalType getVarFinal(VarVersionPair pair) {
    return varVersions == null ? FinalType.FINAL : varVersions.getVarFinal(pair);
  }

  public Pair<String, VarVersionPair> getVarSource(VarVersionPair pair) {
    return varSources.get(pair);
  }

  public void setVarFinal(VarVersionPair pair, FinalType finalType) {
    if (this.varVersions != null) {
      varVersions.setVarFinal(pair, finalType);
    }
  }

  public Map<VarVersionPair, String> getThisVars() {
    return thisVars;
  }

  public Set<VarVersionPair> getExternalVars() {
    return externalVars;
  }

  public List<LocalVariable> getCandidates(int origindex) {
    if (!hasLVT())
        return null;
    return method.getLocalVariableAttr().matchingVars(origindex).collect(Collectors.toList());
  }

  public void findLVT(VarExprent exprent, int start) {
    if (!hasLVT())
      return;

    LocalVariable lvt = method.getLocalVariableAttr().getVariables()
      .filter(v -> v.getVersion().var == exprent.getIndex() && v.getStart() == start).findFirst().orElse(null);

    if (lvt != null) {
      exprent.setLVT(lvt);
    }
  }

  public boolean hasLVT() {
    return method.getLocalVariableAttr() != null;
  }

  public @Nullable VarVersionsProcessor getVarVersions() {
    return varVersions;
  }

  public void setVarLVT(VarVersionPair var, LocalVariable lvt) {
    mapVarLVTs.put(var, lvt);
  }

  public @Nullable LocalVariable getVarLVT(VarVersionPair var) {
    return mapVarLVTs.get(var);
  }
}
