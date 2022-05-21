// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.stats;

import org.jetbrains.java.decompiler.code.cfg.ControlFlowGraph;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.StartEndPair;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.LinkedHashSet;
import java.util.Set;

public final class RootStatement extends Statement {
  private final DummyExitStatement dummyExit;
  public final StructMethod mt;
  public Set<String> commentLines = null;
  public boolean addErrorComment = false;

  public RootStatement(Statement head, DummyExitStatement dummyExit, StructMethod mt) {
    type = StatementType.ROOT;

    first = head;
    this.dummyExit = dummyExit;
    this.mt = mt;

    if (this.first == null) {
      throw new IllegalStateException("Root statement has no content!");
    }

    stats.addWithKey(first, first.id);
    first.setParent(this);
  }

  @Override
  public TextBuffer toJava(int indent) {
    return ExprProcessor.listToJava(varDefinitions, indent).append(first.toJava(indent));
  }

  public DummyExitStatement getDummyExit() {
    return dummyExit;
  }

  public void addComment(String comment) {
    if (commentLines == null) {
      commentLines = new LinkedHashSet<>();
    }

    commentLines.add(comment);
  }

  public void addComments(RootStatement root) {
    if (root.commentLines != null) {
      for (String s : root.commentLines) {
        addComment(s);
      }
    }

    addErrorComment |= root.addErrorComment;
  }

  public void addComments(ControlFlowGraph graph) {
    if (graph.commentLines != null) {
      for (String s : graph.commentLines) {
        addComment(s);
      }
    }

    addErrorComment |= graph.addErrorComment;
  }

  @Override
  public StartEndPair getStartEndRange() {
    return StartEndPair.join(first.getStartEndRange(), dummyExit != null ? dummyExit.getStartEndRange() : null);
  }
}
