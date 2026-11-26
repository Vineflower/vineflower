package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;

import java.util.HashMap;
import java.util.Map;

public final class GroupTracker {
  private final Map<ExprentKey, String> exprentGroups = new HashMap<>();
  private final Map<VarKey, String> varGroups = new HashMap<>();

  public boolean trySetExprentGroup(String containingMethod, Exprent exprent, String groupName) {
    return exprentGroups.putIfAbsent(new ExprentKey(containingMethod, exprent.id), groupName) == null;
  }

  public boolean trySetVarGroup(String containingMethod, VarVersionPair varId, String groupName) {
    return varGroups.putIfAbsent(new VarKey(containingMethod, varId), groupName) == null;
  }

  @Nullable
  public String getExprentGroup(String containingMethod, Exprent exprent) {
    return exprentGroups.get(new ExprentKey(containingMethod, exprent.id));
  }

  @Nullable
  public String getVarGroup(String containingMethod, VarVersionPair varId) {
    return varGroups.get(new VarKey(containingMethod, varId));
  }

  private record ExprentKey(String containingMethod, int exprentId) {
  }

  private record VarKey(String containingMethod, VarVersionPair varId) {
  }
}
