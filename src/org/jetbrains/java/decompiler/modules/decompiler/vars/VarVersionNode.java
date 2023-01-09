// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.IGraphNode;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute.LocalVariable;
import org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect;

import java.util.*;

public class VarVersionNode implements IGraphNode {
  @Deprecated
  public static final int FLAG_PHANTOM_FINEXIT = 2;

  public final int var;

  public final int version;

  public final Set<VarVersionNode> succs2 = new LinkedHashSet<>();
  public final Set<VarVersionNode> preds2 = new LinkedHashSet<>();

  public VarVersionNode phantomNode = null;
  public VarVersionNode phantomParentNode = null;

  public int flags;

  public SFormsFastMapDirect live = null;

  public LocalVariable lvt = null;

  // Debugging
  public State state = null;

  VarVersionNode(int var, int version) {
    this.var = var;
    this.version = version;
  }

  VarVersionNode(int var, int version, LocalVariable lvt) {
    this(var, version);
    this.lvt = lvt;
  }

  @Override
  public List<IGraphNode> getPredecessors() {
    // TODO: does this have to return a list?
    return new ArrayList<>(this.preds2);
  }

  public void removeSuccessor(VarVersionNode node) {
    this.succs2.remove(node);
  }

  public void removePredecessor(VarVersionNode node) {
    this.preds2.remove(node);
  }

  public boolean hasSinglePredecessor() {
    return this.preds2.size() == 1;
  }

  public VarVersionNode getSinglePredecessor() {
    ValidationHelper.assertTrue(this.hasSinglePredecessor(), "Expected only a single predecessor");
    return this.preds2.iterator().next();
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
    WRITE, READ, PHI, DEAD_READ, PHANTOM
  }
}