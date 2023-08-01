// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.flow;

import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;

import java.util.*;


public class DirectNode {
  public final DirectNodeType type;

  public final String id;

  public BasicBlockStatement block;

  public final Statement statement;

  public List<Exprent> exprents = new ArrayList<>();

  private final Map<DirectEdgeType, List<DirectEdge>> successors = new HashMap<>();
  private final Map<DirectEdgeType, List<DirectEdge>> predecessors = new HashMap<>();
  public final DirectNode tryFinally;

  private DirectNode(DirectNodeType type, Statement statement, DirectNode tryFinally) {
    this.type = type;
    this.statement = statement;
    this.tryFinally = tryFinally;
    this.id = type.makeId(statement.id);
  }

  public static DirectNode forStat(
    DirectNodeType type,
    Statement statement,
    DirectNode tryFinally
  ) {
    return new DirectNode(type, statement, tryFinally);
  }

  public boolean hasSuccessors(DirectEdgeType type) {
    return this.successors.containsKey(type) && !this.successors.get(type).isEmpty();
  }
  public List<DirectEdge> getSuccessors(DirectEdgeType type) {
    return this.successors.computeIfAbsent(type, t -> new ArrayList<>());
  }

  public boolean hasPredecessors(DirectEdgeType type) {
    return this.predecessors.containsKey(type) && !this.predecessors.get(type).isEmpty();
  }
  public List<DirectEdge> getPredecessors(DirectEdgeType type) {
    return this.predecessors.computeIfAbsent(type, t -> new ArrayList<>());
  }

  public void addSuccessor(DirectEdge edge) {
    ValidationHelper.validateTrue(edge.getSource() == this, "Source node mismatch");
    if (!getSuccessors(edge.getType()).contains(edge)) {
      getSuccessors(edge.getType()).add(edge);
    }

    if (!edge.getDestination().getPredecessors(edge.getType()).contains(edge)) {
      edge.getDestination().getPredecessors(edge.getType()).add(edge);
    }
  }

  @Deprecated
  public List<DirectNode> succs() {
    List<DirectNode> list = new ArrayList<>();
    for (DirectEdge edge : getSuccessors(DirectEdgeType.REGULAR)) {
      DirectNode destination = edge.getDestination();
      list.add(destination);
    }

    return list;
  }

  @Deprecated
  public List<DirectNode> preds() {
    List<DirectNode> list = new ArrayList<>();
    for (DirectEdge edge : getPredecessors(DirectEdgeType.REGULAR)) {
      DirectNode source = edge.getSource();
      list.add(source);
    }

    return list;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DirectNode that = (DirectNode) o;
    return type == that.type && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, id);
  }

  @Override
  public String toString() {
    return id;
  }
}
