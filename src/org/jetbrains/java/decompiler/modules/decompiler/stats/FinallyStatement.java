// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.stats;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.List;

public class FinallyStatement extends Statement {
  private Statement handler;

  // *****************************************************************************
  // constructors
  // *****************************************************************************

  protected FinallyStatement() {
    super(StatementType.FINALLY);
  }

  public FinallyStatement(Statement head, Statement handler) {
    this();

    first = head;
    stats.addWithKey(head, head.id);
    head.setParent(this);

    this.handler = handler;
    stats.addWithKey(handler, handler.id);
    handler.setParent(this);
  }


  // *****************************************************************************
  // public methods
  // *****************************************************************************

  @Override
  public TextBuffer toJava(int indent) {
    String new_line_separator = DecompilerContext.getNewLineSeparator();

    TextBuffer buf = new TextBuffer();

    buf.append(ExprProcessor.listToJava(varDefinitions, indent));

    boolean labeled = isLabeled();
    if (labeled) {
      buf.appendIndent(indent).append("label").append(this.id).append(":").appendLineSeparator();
    }

    List<StatEdge> lstSuccs = first.getSuccessorEdges(STATEDGE_DIRECT_ALL);
    if (first instanceof CatchStatement && first.varDefinitions.isEmpty() &&
      !labeled && !first.isLabeled() && (lstSuccs.isEmpty() || !lstSuccs.get(0).explicit)) {
      TextBuffer content = ExprProcessor.jmpWrapper(first, indent, true);
      content.setLength(content.length() - new_line_separator.length());
      buf.append(content);
    } else {
      buf.appendIndent(indent).append("try {").appendLineSeparator();
      buf.append(ExprProcessor.jmpWrapper(first, indent + 1, true));
      buf.appendIndent(indent).append("}");
    }

    buf.append(" finally {").appendLineSeparator();

    buf.append(ExprProcessor.jmpWrapper(handler, indent + 1, true));

    buf.appendIndent(indent).append("}").appendLineSeparator();

    return buf;
  }

  @Override
  public void replaceStatement(Statement oldstat, Statement newstat) {

    if (handler == oldstat) {
      handler = newstat;
    }

    super.replaceStatement(oldstat, newstat);
  }

  @Override
  public Statement getSimpleCopy() {
    return new FinallyStatement();
  }

  @Override
  public void initSimpleCopy() {
    first = stats.get(0);
    handler = stats.get(1);
  }

  // *****************************************************************************
  // getter and setter methods
  // *****************************************************************************

  public Statement getHandler() {
    return handler;
  }
}