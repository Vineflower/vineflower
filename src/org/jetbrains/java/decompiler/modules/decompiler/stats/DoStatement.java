// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.stats;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Pattern;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// Loop statement
public class DoStatement extends Statement {
  public enum Type {
    INFINITE, DO_WHILE, WHILE, FOR, FOR_EACH
  }

  private Type looptype;

  private final List<Exprent> initExprent = new ArrayList<>();
  private final List<Exprent> conditionExprent = new ArrayList<>();
  private final List<Exprent> incExprent = new ArrayList<>();

  // *****************************************************************************
  // constructors
  // *****************************************************************************

  protected DoStatement() {
    super(StatementType.DO);
    looptype = Type.INFINITE;

    initExprent.add(null);
    conditionExprent.add(null);
    incExprent.add(null);
  }

  protected DoStatement(Statement head) {

    this();

    first = head;
    stats.addWithKey(first, first.id);

    // post is always null!
  }

  // *****************************************************************************
  // public methods
  // *****************************************************************************

  public static Statement isHead(Statement head) {

    if (head.getLastBasicType() == LastBasicType.GENERAL && !head.isMonitorEnter()) {

      // at most one outgoing edge
      StatEdge edge = null;
      List<StatEdge> lstSuccs = head.getSuccessorEdges(STATEDGE_DIRECT_ALL);
      if (!lstSuccs.isEmpty()) {
        edge = lstSuccs.get(0);
      }

      // regular loop
      if (edge != null && edge.getType() == StatEdge.TYPE_REGULAR && edge.getDestination() == head) {
        return new DoStatement(head);
      }

      // continues
      if (!(head instanceof DoStatement) && (edge == null || edge.getType() != StatEdge.TYPE_REGULAR) &&
          head.getContinueSet().contains(head.getBasichead())) {
        return new DoStatement(head);
      }
    }

    return null;
  }

  @Override
  public HashSet<Statement> buildContinueSet() {
    super.buildContinueSet();

    continueSet.remove(first.getBasichead());

    return continueSet;
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();

    buf.append(ExprProcessor.listToJava(varDefinitions, indent));

    if (isLabeled()) {
      appendLabel(buf, indent);
    }

    switch (looptype) {
      case INFINITE:
        buf.appendIndent(indent).appendKeyword("while").appendWhitespace(" ").appendPunctuation("(").appendKeyword("true").appendPunctuation(")").appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(first, indent + 1, false));
        buf.appendIndent(indent).appendPunctuation("}").appendLineSeparator();
        break;
      case DO_WHILE:
        buf.appendIndent(indent).appendKeyword("do").appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(first, indent + 1, false));
        buf.appendIndent(indent).appendPunctuation("}").appendWhitespace(" ").appendKeyword("while").appendWhitespace(" ").appendPunctuation("(");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        buf.append(conditionExprent.get(0).toJava(indent));
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.appendPunctuation(");").appendLineSeparator();
        break;
      case WHILE:
        buf.appendIndent(indent).appendKeyword("while").appendWhitespace(" ").appendPunctuation("(");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        buf.append(conditionExprent.get(0).toJava(indent));
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.appendPunctuation(")").appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(first, indent + 1, false));
        buf.appendIndent(indent).appendPunctuation("}").appendLineSeparator();
        break;
      case FOR:
        buf.appendIndent(indent);
        buf.pushNewlineGroup(indent, 1);
        buf.appendKeyword("for").appendWhitespace(" ").appendPunctuation("(");
        if (initExprent.get(0) != null) {
          buf.append(initExprent.get(0).toJava(indent));
        }
        buf.appendPunctuation(";").appendPossibleNewline(" ")
          .append(conditionExprent.get(0).toJava(indent)).appendPunctuation(";").appendPossibleNewline(" ")
          .append(incExprent.get(0).toJava(indent))
          .appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.appendPunctuation(")").appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(first, indent + 1, false));
        buf.appendIndent(indent).appendPunctuation("}").appendLineSeparator();
        break;
      case FOR_EACH:
        buf.appendIndent(indent).appendKeyword("for").appendWhitespace(" ").appendPunctuation("(").append(initExprent.get(0).toJava(indent));
        incExprent.get(0).getInferredExprType(null); //TODO: Find a better then null? For now just calls it to clear casts if needed
        buf.appendWhitespace(" ").appendPunctuation(":").appendWhitespace(" ").append(incExprent.get(0).toJava(indent)).appendPunctuation(")").appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(first, indent + 1, true));
        buf.appendIndent(indent).appendPunctuation("}").appendLineSeparator();
    }

    return buf;
  }

  @Override
  public List<Exprent> getStatExprents() {
    List<Exprent> lst = new ArrayList<>();

    switch (looptype) {
      case FOR:
        if (getInitExprent() != null) {
          lst.add(getInitExprent());
        }
      case WHILE:
        lst.add(getConditionExprent());
        break;
      case FOR_EACH:
        lst.add(getInitExprent());
        lst.add(getIncExprent());
    }

    switch (looptype) {
      case DO_WHILE:
        lst.add(getConditionExprent());
        break;
      case FOR:
        lst.add(getIncExprent());
    }

    return lst;
  }

  @Override
  public void replaceExprent(Exprent oldexpr, Exprent newexpr) {
    if (initExprent.get(0) == oldexpr) {
      initExprent.set(0, newexpr);
    }
    if (conditionExprent.get(0) == oldexpr) {
      conditionExprent.set(0, newexpr);
    }
    if (incExprent.get(0) == oldexpr) {
      incExprent.set(0, newexpr);
    }
  }

  @Override
  public List<VarExprent> getImplicitlyDefinedVars() {
    List<VarExprent> vars = new ArrayList<>();


    if (looptype == Type.FOR_EACH) {
      getPatterns(getIncExprent(), vars);
      return vars;
    }

    if (getConditionExprent() == null) {
      return null;
    }

    getPatterns(getConditionExprent(), vars);

    return vars;
  }

  private static void getPatterns(Exprent cond, List<VarExprent> vars) {
    List<Exprent> conditionList = cond.getAllExprents(true, true);

    for (Exprent condition : conditionList) {
      if (condition instanceof FunctionExprent func) {

        // Pattern match variable is implicitly defined
        if (func.getFuncType() == FunctionType.INSTANCEOF && func.getLstOperands().size() > 2) {
          vars.addAll(((Pattern) func.getLstOperands().get(2)).getPatternVars());
        }
      }
    }
  }

  @Override
  public boolean hasBasicSuccEdge() {
    return looptype != Type.INFINITE;
  }

  @Override
  public Statement getSimpleCopy() {
    return new DoStatement();
  }

  // *****************************************************************************
  // getter and setter methods
  // *****************************************************************************

  public List<Exprent> getInitExprentList() {
    return initExprent;
  }

  public List<Exprent> getConditionExprentList() {
    return conditionExprent;
  }

  public List<Exprent> getIncExprentList() {
    return incExprent;
  }

  public Exprent getConditionExprent() {
    return conditionExprent.get(0);
  }

  public void setConditionExprent(Exprent conditionExprent) {
    this.conditionExprent.set(0, conditionExprent);
  }

  public Exprent getIncExprent() {
    return incExprent.get(0);
  }

  public void setIncExprent(Exprent incExprent) {
    this.incExprent.set(0, incExprent);
  }

  public Exprent getInitExprent() {
    return initExprent.get(0);
  }

  public void setInitExprent(Exprent initExprent) {
    this.initExprent.set(0, initExprent);
  }

  public Type getLooptype() {
    return looptype;
  }

  public void setLooptype(Type looptype) {
    this.looptype = looptype;
  }
}
