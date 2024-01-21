// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.IGraphNode;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute.LocalVariable;
import org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect;

import java.util.*;

public class VarVersionNode implements IGraphNode {

  public final int var;
  public final int version;

  public final Set<VarVersionNode> successors = new LinkedHashSet<>();
  public final Set<VarVersionNode> predecessors = new LinkedHashSet<>();

  public VarVersionNode phantomNode = null;
  public VarVersionNode phantomParentNode = null;

  public SFormsFastMapDirect live = null;

  public LocalVariable lvt;

  // Debugging & Validation
  public State state = null;

  VarVersionNode(int var, int version, LocalVariable lvt) {
    this.var = var;
    this.version = version;
    this.lvt = lvt;
  }

  @Override
  public Collection<VarVersionNode> getPredecessors() {
    return this.predecessors;
  }

  public void removeSuccessor(VarVersionNode node) {
    this.successors.remove(node);
  }

  public void removePredecessor(VarVersionNode node) {
    this.predecessors.remove(node);
  }

  public boolean hasSinglePredecessor() {
    return this.predecessors.size() == 1;
  }

  public boolean hasAnySuccessors() {
    return !this.successors.isEmpty();
  }

  public VarVersionNode getSinglePredecessor() {
    ValidationHelper.validateTrue(this.hasSinglePredecessor(), "Expected only a single predecessor");
    return this.predecessors.iterator().next();
  }

  @Override
  public String toString() {
    return "(" + this.var + "_" + this.version + ")";
  }

  public VarVersionPair asPair() {
    return new VarVersionPair(this.var, this.version);
  }

  /**
   * Just for debugging
   */
  public enum State {
    WRITE, READ, PHI, DEAD_READ, PHANTOM, PARAM, CATCH
  }
}