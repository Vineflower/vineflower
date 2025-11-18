// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.sforms;

import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionNode;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsGraph;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory;
import org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect;

import java.util.*;

public class SSAUConstructorSparseEx extends SFormsConstructor {
  // track assignments for finding effectively final vars (left var, right var)
  private final Map<VarVersionPair, VarVersionPair> varAssignmentMap = new HashMap<>();

  // version, protected ranges (catch, finally)
  private final Map<VarVersionPair, Integer> mapVersionFirstRange = new HashMap<>();

  // versions memory dependencies
  final VarVersionsGraph ssuVersions = new VarVersionsGraph();

  // field access vars (exprent id, var id)
  private final Map<Integer, Integer> mapFieldVars = new HashMap<>();

  // field access counter
  private int fieldVarCounter = -1;

  public SSAUConstructorSparseEx() {
    super(
      true,
      true
    );
  }

  @Override
  public void splitVariables(RootStatement root, StructMethod mt) {
    super.splitVariables(root, mt);

    this.ssaStatements(this.dgraph, new HashSet<>(), true, mt, 999_999);

    this.ssuVersions.initDominators();

    // Validation testing
    ValidationHelper.validateVarVersionsGraph(this.ssuVersions, root, this.varAssignmentMap);
  }

  public VarVersionNode getNode(VarExprent var) {
    return this.getNode(var.getVarVersionPair());
  }

  private VarVersionNode getNode(VarVersionPair pair) {
    return this.ssuVersions.nodes.getWithKey(pair);
  }

  private VarVersionNode getNode(int var, int version) {
    return this.getNode(new VarVersionPair(var, version));
  }


  @Override
  public VarVersionPair getOrCreatePhantom(VarVersionPair pair) {
    VarVersionNode varNode = this.getNode(pair);
    if (varNode.phantomNode == null) {
      VarVersionNode phantomNode = this.createNewNode(varNode.var, null, VarVersionNode.State.PHANTOM);
      varNode.phantomNode = phantomNode;
      phantomNode.phantomParentNode = varNode;
      return phantomNode.asPair();
    }

    ValidationHelper.validateTrue(
      varNode.phantomNode.state == VarVersionNode.State.PHANTOM,
      "Expected phantom node to be PHANTOM");

    return varNode.phantomNode.asPair();
  }

  @Override
  public void markDirectAssignment(VarVersionPair varVersionPair, VarVersionPair rightPair) {
    this.varAssignmentMap.put(varVersionPair, rightPair);
  }

  @Override
  void varReadSingleVersion(
    Statement stat,
    boolean calcLiveVars,
    VarExprent varExprent,
    SFormsFastMapDirect varMap,
    int lastVersion) {

    int varIndex = varExprent.getIndex();
    int currentVersion = varExprent.getVersion();

    if (currentVersion == 0) {
      // first time processing this exprent

      // ssu graph
      VarVersionNode previousNode = this.getNode(varIndex, lastVersion);
      VarVersionNode useNode = this.createRead(previousNode, stat);

      // set version
      varExprent.setVersion(useNode.version);
    }

    this.updateLiveMap(new VarVersionPair(varIndex, currentVersion), varMap, calcLiveVars);
    varMap.setCurrentVar(varExprent); // update the current var to the usage version
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

    if (currentVersion == 0) { // first time processing this exprent
      VarVersionNode node = this.createNewNode(varIndex, stat, VarVersionNode.State.PHI);
      currentVersion = node.version;

      // set version
      varExprent.setVersion(currentVersion);
    }
    this.updateLiveMap(new VarVersionPair(varIndex, currentVersion), varMap, calcLiveVars);

    varMap.setCurrentVar(varExprent); // update varmap to the usage version
    this.createOrUpdatePhiNode(new VarVersionPair(varIndex, currentVersion), versions, stat);
  }

  // SSAU only
  private void createOrUpdatePhiNode(VarVersionPair phivar, FastSparseSetFactory.FastSparseSet<Integer> vers, Statement stat) {
    Set<Integer> oldNodes = new HashSet<>();

    // ssu graph
    VarVersionNode phiNode = this.ssuVersions.nodes.getWithKey(phivar);
    ValidationHelper.validateTrue(phiNode.phantomParentNode == null, "phi node can't be a phantom node");
    if (phiNode.predecessors.isEmpty()) {
      ValidationHelper.validateTrue(
        phiNode.state == VarVersionNode.State.PHI,
        "Phi node has the wrong state?");
    } else if (phiNode.predecessors.size() == 1) {
      // not yet a phi node
      ValidationHelper.validateTrue(
        phiNode.state == VarVersionNode.State.READ,
        "Trying to convert a non read node into a phi node");
      phiNode.state = VarVersionNode.State.PHI;
      phiNode.getSinglePredecessor().removeSuccessor(phiNode);
      phiNode.predecessors.clear();
    } else {
      ValidationHelper.validateTrue(
        phiNode.state == VarVersionNode.State.PHI,
        "Phi node has the wrong state?");
      for (Iterator<VarVersionNode> iterator = phiNode.predecessors.iterator(); iterator.hasNext(); ) {
        VarVersionNode source = iterator.next();
        ValidationHelper.validateTrue(
          source.state == VarVersionNode.State.READ,
          "Phi node is reading from a non READ node");

        // source is the read node, the version in the varmap is the version it read from
        int verssrc = source.getSinglePredecessor().version;
        if (!vers.contains(verssrc)) {
          source.removeSuccessor(phiNode);
          iterator.remove();
          source.state = VarVersionNode.State.DEAD_READ;
        } else {
          oldNodes.add(verssrc);
        }
      }
    }

    for (int ver : vers) {
      if (oldNodes.contains(ver)) {
        continue;
      }

      VarVersionNode preNode = this.ssuVersions.nodes.getWithKey(new VarVersionPair(phivar.var, ver));
      VarVersionNode tempNode = this.createRead(preNode, stat);
      makeReadEdge(phiNode, tempNode);
    }
  }


  private VarVersionNode createRead(VarVersionNode node, Statement stat) {
    VarVersionNode read = this.createNewNode(node.var, stat, VarVersionNode.State.READ);
    makeReadEdge(read, node);
    return read;
  }

  private VarVersionNode createNewNode(int var, Statement stat, VarVersionNode.State state) {
    int version = this.getNextFreeVersion(var, stat);
    VarVersionNode node = this.ssuVersions.createNode(new VarVersionPair(var, version));
    node.state = state;
    return node;
  }


  @Override
  public Integer getFieldIndex(FieldExprent field) {
    if (this.mapFieldVars.containsKey(field.id)) {
      return this.mapFieldVars.get(field.id);
    } else {
      int index = this.fieldVarCounter--;
      this.mapFieldVars.put(field.id, index);

      // ssu graph
      this.ssuVersions.createNode(new VarVersionPair(index, 1));
      return index;
    }
  }

  @Override
  protected int getNextFreeVersion(int var, Statement stat) {
    int nextVersion = super.getNextFreeVersion(var, stat);

    // save the first protected range, containing current statement
    if (stat != null) { // null iff phantom version
      Statement firstRange = getFirstProtectedRange(stat);

      if (firstRange != null) {
        this.mapVersionFirstRange.put(new VarVersionPair(var, nextVersion), firstRange.id);
      }
    }

    return nextVersion;
  }


  public VarVersionsGraph getSsuVersions() {
    return this.ssuVersions;
  }

  public SFormsFastMapDirect getLiveVarVersionsMap(VarVersionPair varVersion) {
    VarVersionNode node = this.ssuVersions.nodes.getWithKey(varVersion);
    if (node != null) {
      return node.live == null ? new SFormsFastMapDirect(this.factory) : node.live;
    }

    return null;
  }

  public Map<VarVersionPair, Integer> getMapVersionFirstRange() {
    return this.mapVersionFirstRange;
  }

  public Map<Integer, Integer> getMapFieldVars() {
    return this.mapFieldVars;
  }

  public Map<VarVersionPair, VarVersionPair> getVarAssignmentMap() {
    return this.varAssignmentMap;
  }

  @Override
  protected void onAssignment(VarVersionPair varVersionPair, SFormsFastMapDirect varMap, boolean calcLiveVars) {
    this.updateLiveMap(varVersionPair, varMap, calcLiveVars);
  }

  private void updateLiveMap(VarVersionPair varVersionPair, SFormsFastMapDirect varMap, boolean calcLiveVars) {
    if (calcLiveVars) {
      VarVersionNode node = this.getNode(varVersionPair);

      node.live = varMap.getCopy();
    }
  }

  @Override
  void initVersion(VarExprent varExprent, Statement stat) {
    if (varExprent.getVersion() == 0) {
      VarVersionNode node = this.createNewNode(varExprent.getIndex(), stat, VarVersionNode.State.WRITE);

      // set version
      varExprent.setVersion(node.version);
    }
  }

  @Override
  public void initParameter(int varIndex, SFormsFastMapDirect varMap, boolean isCatch) {
    VarVersionNode node = this.createNewNode(
      varIndex, this.root, isCatch ? VarVersionNode.State.CATCH : VarVersionNode.State.PARAM);

    varMap.setCurrentVar(node);
  }
}
