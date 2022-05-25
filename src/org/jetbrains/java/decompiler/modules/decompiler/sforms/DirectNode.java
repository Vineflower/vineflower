// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.sforms;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;

import java.util.ArrayList;
import java.util.List;


public class DirectNode {
  public final NodeType type;

  public final String id;

  public BasicBlockStatement block;

  public final Statement statement;

  public List<Exprent> exprents = new ArrayList<>();

  public final List<DirectNode> succs = new ArrayList<>();

  public final List<DirectNode> preds = new ArrayList<>();

  private DirectNode(NodeType type, Statement statement) {
    this.type = type;
    this.statement = statement;
    this.id = type.makeId(statement.id);
  }

  public static DirectNode forStat(NodeType type, Statement statement) {
    return new DirectNode(type, statement);
  }

  private DirectNode(NodeType type, Statement statement, BasicBlockStatement block) {
    this.type = type;
    this.statement = statement;

    this.id = Integer.toString(block.id);
    this.block = block;
  }

  public static DirectNode forBlock(Statement statement) {
    return new DirectNode(NodeType.DIRECT, statement, (BasicBlockStatement)statement);
  }

  @Override
  public String toString() {
    return id;
  }

  public enum NodeType {
    DIRECT("") {
      @Override
      protected String makeId(int statId) {
        return "" + statId;
      }
    },
    TAIL("tail"),
    INIT("init"),
    CONDITION("cond"),
    INCREMENT("inc"),
    TRY("try"),
    FOREACH_VARDEF("foreach");

    private final String name;

    NodeType(String name) {
      this.name = name;
    }

    protected String makeId(int statId) {
      return statId + "_" + name;
    }
  }
}
