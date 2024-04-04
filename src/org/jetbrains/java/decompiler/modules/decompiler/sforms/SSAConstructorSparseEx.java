// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.sforms;

import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory;
import org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect;

import java.util.HashMap;
import java.util.Map;

public class SSAConstructorSparseEx extends SFormsConstructor {

  // (var, version), version
  private final Map<VarVersionPair, FastSparseSetFactory.FastSparseSet<Integer>> phi = new HashMap<>();

  public SSAConstructorSparseEx() {
    super(
      false,
      false
    );
  }

  @Override
  public void markDirectAssignment(VarVersionPair varVersionPair, VarVersionPair rightPair) {
  }

  @Override
  public VarVersionPair getOrCreatePhantom(VarVersionPair pair) {
    return pair;
  }

  @Override
  public Integer getFieldIndex(FieldExprent field) {
    return -1;
  }

  @Override
  void varReadSingleVersion(
    Statement stat,
    boolean calcLiveVars,
    VarExprent varExprent,
    SFormsFastMapDirect varmap,
    int lastVersion) {
    // simply copy the version
    varExprent.setVersion(lastVersion);
  }

  @Override
  void varReadMultipleVersions(
    Statement stat,
    boolean calcLiveVars,
    VarExprent varExprent,
    SFormsFastMapDirect varMap,
    FastSparseSetFactory.FastSparseSet<Integer> versions) {

    int varIndex = varExprent.getIndex();
    int currentVersion = varExprent.getVersion();
    VarVersionPair varVersion = varExprent.getVarVersionPair();
    if (currentVersion != 0 && this.phi.containsKey(varVersion)) {
      // keep phi node up to date of all inputs
      this.phi.get(varVersion).union(versions);
    } else {
      // increase version
      int nextVer = this.getNextFreeVersion(varIndex, stat);
      // set version
      varExprent.setVersion(nextVer);

      // create new phi node
      this.phi.put(varExprent.getVarVersionPair(), versions);
    }

    varMap.setCurrentVar(varExprent); // update varMap to the phi version
  }

  public Map<VarVersionPair, FastSparseSetFactory.FastSparseSet<Integer>> getPhi() {
    return this.phi;
  }

  public Map<VarVersionPair, Integer> getSimpleReversePhiLookup() {
    // simple union find
    Map<VarVersionPair, Integer> ret = new HashMap<>();
    for (var entry : this.phi.entrySet()) {
      int index = entry.getKey().var;
      VarVersionPair left = entry.getKey();

      while (ret.containsKey(left)) {
        int version = ret.get(left);
        if (version == left.version) {
          break;
        }
        left = new VarVersionPair(index, version);
      }

      for (int ver : entry.getValue()) {
        VarVersionPair right = new VarVersionPair(index, ver);

        while (ret.containsKey(right)) {
          int version = ret.get(right);
          if (version == right.version) {
            break;
          }
          right = new VarVersionPair(index, version);
        }

        if (left.version != right.version) {
          if (right.version < left.version) {
            ret.put(left, right.version);
            left = right;
          } else {
            ret.put(right, left.version);
          }
        }
      }
    }

    // fully flatten each version
    for (var entry : this.phi.entrySet()) {
      VarVersionPair pair = entry.getKey();

      while (ret.containsKey(pair)) {
        int version = ret.get(pair);
        if (version == pair.version) {
          break;
        }
        pair = new VarVersionPair(pair.var, version);
      }

      ret.put(entry.getKey(), pair.version);
    }

    return ret;
  }

  @Override
  void initVersion(VarExprent varExprent, Statement stat) {
    if (varExprent.getVersion() == 0) {
      // get next version
      int nextVersion = this.getNextFreeVersion(varExprent.getIndex(), stat);

      // set version
      varExprent.setVersion(nextVersion);
    }
  }

  @Override
  public void initParameter(int varIndex, SFormsFastMapDirect varMap, boolean isCatchVar)  {
    int version = this.getNextFreeVersion(varIndex, this.root); // == 1

    varMap.setCurrentVar(varIndex, version);
  }
}
