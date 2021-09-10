// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.java.decompiler.main.collectors.VarNamesCollector;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute.LocalVariable;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
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
  private Map<VarVersionPair, LocalVariable> mapVarLVTs = new HashMap<>();
  private VarVersionsProcessor varVersions;
  private final Map<VarVersionPair, String> thisVars = new HashMap<>();
  private final Set<VarVersionPair> externalVars = new HashSet<>();
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

  public void setVarDefinitions(Statement root) {
    mapVarNames = new HashMap<>();
    new VarDefinitionHelper(root, method, this).setVarDefinitions();
  }

  public void setDebugVarNames(Map<VarVersionPair, String> mapDebugVarNames) {
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
        if (debugName != null && TextUtil.isValidIdentifier(debugName, method.getBytecodeVersion())) {
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
  }

  public Integer getVarOriginalIndex(int index) {
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

  public VarNamesCollector getVarNamesCollector() {
    return varNamesCollector;
  }

  public VarType getVarType(VarVersionPair pair) {
    return varVersions == null ? null : varVersions.getVarType(pair);
  }

  public void setVarType(VarVersionPair pair, VarType type) {
    if (varVersions != null) {
      varVersions.setVarType(pair, type);
    }
  }

  public String getVarName(VarVersionPair pair) {
    return mapVarNames == null ? null : mapVarNames.get(pair);
  }

  public void setVarName(VarVersionPair pair, String name) {
    mapVarNames.put(pair, name);
  }

  public Set<VarVersionPair> getUsedVarVersions() {
    return mapVarNames != null ? mapVarNames.keySet() : Collections.emptySet();
  }

  public Collection<String> getVarNames() {
    return mapVarNames != null ? mapVarNames.values() : Collections.emptySet();
  }

  public int getVarFinal(VarVersionPair pair) {
    return varVersions == null ? VarTypeProcessor.VAR_FINAL : varVersions.getVarFinal(pair);
  }

  public void setVarFinal(VarVersionPair pair, int finalType) {
    varVersions.setVarFinal(pair, finalType);
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

  public void copyVarInfo(VarVersionPair from, VarVersionPair to) {
    setVarName(to, getVarName(from));
    setVarFinal(to, getVarFinal(from));
    setVarType(to, getVarType(from));
    varVersions.getMapOriginalVarIndices().put(to.var, varVersions.getMapOriginalVarIndices().get(from.var));
  }

  public boolean hasLVT() {
    return method.getLocalVariableAttr() != null;
  }
  

  public Map<Integer, LocalVariable> getLocalVariables(Statement stat) {
    if (!hasLVT() || stat == null)
      return new HashMap<>();

    final StartEndPair sep = stat.getStartEndRange(); 
    final Set<Integer> blacklist = new HashSet<>();
    Map<Integer, LocalVariable> ret = method.getLocalVariableAttr().getVariables().filter(lv -> lv.getEnd() > sep.start && lv.getStart() <= sep.end)
      .collect(Collectors.toMap(lv -> lv.getVersion().var, lv -> lv,
        (lv1, lv2) -> 
        {
          //System.out.println("DUPLICATE INDEX FOR SCOPE: (" +sep +") " + lv1.toString() + " " + lv2.toString());
          blacklist.add(lv1.getVersion().var);
          return lv1;
        }
      ));

    for (Integer b : blacklist)
      ret.remove(b);

    return ret;
  }

  public VarVersionsProcessor getVarVersions() {
    return varVersions;
  }

  public void setVarLVT(VarVersionPair var, LocalVariable lvt) {
    mapVarLVTs.put(var, lvt);
  }

  public LocalVariable getVarLVT(VarVersionPair var) {
    return mapVarLVTs.get(var);
  }
}
