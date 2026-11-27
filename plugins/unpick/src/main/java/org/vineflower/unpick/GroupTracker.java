package org.vineflower.unpick;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class GroupTracker {
  private final Map<ExprentKey, Set<String>> exprentGroups = new HashMap<>();
  private final Map<VarKey, Set<String>> varGroups = new HashMap<>();

  public boolean tryAddExprentGroup(String containingMethod, Exprent exprent, String groupName) {
    return exprentGroups.computeIfAbsent(new ExprentKey(containingMethod, exprent.id), k -> new LinkedHashSet<>()).add(groupName);
  }

  public boolean tryAddExprentGroups(String containingMethod, Exprent exprent, Collection<String> groupNames) {
    if (groupNames.isEmpty()) {
      return false;
    }

    return exprentGroups.computeIfAbsent(new ExprentKey(containingMethod, exprent.id), k -> new LinkedHashSet<>()).addAll(groupNames);
  }

  public boolean tryAddVarGroup(String containingMethod, VarVersionPair varId, String groupName) {
    return varGroups.computeIfAbsent(new VarKey(containingMethod, varId), k -> new LinkedHashSet<>()).add(groupName);
  }

  public boolean tryAddVarGroups(String containingMethod, VarVersionPair varId, Collection<String> groupNames) {
    if (groupNames.isEmpty()) {
      return false;
    }

    return varGroups.computeIfAbsent(new VarKey(containingMethod, varId), k -> new LinkedHashSet<>()).addAll(groupNames);
  }

  public Set<String> getExprentGroups(String containingMethod, Exprent exprent) {
    return exprentGroups.getOrDefault(new ExprentKey(containingMethod, exprent.id), Set.of());
  }

  public Set<String> getVarGroups(String containingMethod, VarVersionPair varId) {
    return varGroups.getOrDefault(new VarKey(containingMethod, varId), Set.of());
  }

  private record ExprentKey(String containingMethod, int exprentId) {
  }

  private record VarKey(String containingMethod, VarVersionPair varId) {
  }
}
