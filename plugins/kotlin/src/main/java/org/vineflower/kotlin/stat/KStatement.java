package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.match.IMatchable;
import org.jetbrains.java.decompiler.struct.match.MatchEngine;
import org.jetbrains.java.decompiler.struct.match.MatchNode;
import org.jetbrains.java.decompiler.util.StartEndPair;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class KStatement<T extends Statement> extends Statement {
  protected final T statement;

  public KStatement(T statement) {
    super(statement.type);
    this.statement = statement;
  }

  public abstract TextBuffer toJava(int indent);

  @Override
  public void clearTempInformation() {
    statement.clearTempInformation();
  }

  @Override
  public void collapseNodesToStatement(Statement stat) {
    statement.collapseNodesToStatement(stat);
  }

  @Override
  public void addLabeledEdge(StatEdge edge) {
    statement.addLabeledEdge(edge);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void addEdgeInternal(EdgeDirection direction, StatEdge edge) {
    statement.addEdgeInternal(direction, edge);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void removeEdgeInternal(EdgeDirection direction, StatEdge edge) {
    statement.removeEdgeInternal(direction, edge);
  }

  @Override
  public void addPredecessor(StatEdge edge) {
    statement.addPredecessor(edge);
  }

  @Override
  public void removePredecessor(StatEdge edge) {
    statement.removePredecessor(edge);
  }

  @Override
  public void addSuccessor(StatEdge edge) {
    statement.addSuccessor(edge);
  }

  @Override
  public void removeSuccessor(StatEdge edge) {
    statement.removeSuccessor(edge);
  }

  @Override
  public void removeAllSuccessors(Statement stat) {
    statement.removeAllSuccessors(stat);
  }

  @Override
  public HashSet<Statement> buildContinueSet() {
    return statement.buildContinueSet();
  }

  @Override
  public void buildMonitorFlags() {
    statement.buildMonitorFlags();
  }

  @Override
  public void markMonitorexitDead() {
    statement.markMonitorexitDead();
  }

  @Override
  public List<Statement> getReversePostOrderList(Statement root) {
    return statement.getReversePostOrderList(root);
  }

  @Override
  public List<Statement> getPostReversePostOrderList(List<Statement> lstexits) {
    return statement.getPostReversePostOrderList(lstexits);
  }

  @Override
  public boolean containsStatement(Statement stat) {
    return statement.containsStatement(stat);
  }

  @Override
  public boolean containsStatementStrict(Statement stat) {
    return statement.containsStatementStrict(stat);
  }

  @Override
  public List<Object> getSequentialObjects() {
    return statement.getSequentialObjects();
  }

  @Override
  public void initExprents() {
    statement.initExprents();
  }

  @Override
  public void replaceExprent(Exprent oldexpr, Exprent newexpr) {
    statement.replaceExprent(oldexpr, newexpr);
  }

  @Override
  public Statement getSimpleCopy() {
    return statement.getSimpleCopy();
  }

  @Override
  public void initSimpleCopy() {
    statement.initSimpleCopy();
  }

  @Override
  public void replaceStatement(Statement oldstat, Statement newstat) {
    statement.replaceStatement(oldstat, newstat);
  }

  @Override
  public List<VarExprent> getImplicitlyDefinedVars() {
    return statement.getImplicitlyDefinedVars();
  }

  @Override
  public void changeEdgeNode(EdgeDirection direction, StatEdge edge, Statement value) {
    statement.changeEdgeNode(direction, edge, value);
  }

  @Override
  public void changeEdgeType(EdgeDirection direction, StatEdge edge, int newtype) {
    statement.changeEdgeType(direction, edge, newtype);
  }

  @Override
  public List<Statement> getNeighbours(int type, EdgeDirection direction) {
    return statement.getNeighbours(type, direction);
  }

  @Override
  public Set<Statement> getNeighboursSet(int type, EdgeDirection direction) {
    return statement.getNeighboursSet(type, direction);
  }

  @Override
  public List<StatEdge> getSuccessorEdges(int type) {
    return statement.getSuccessorEdges(type);
  }

  @Override
  public List<StatEdge> getSuccessorEdgeView(int type) {
    return statement.getSuccessorEdgeView(type);
  }

  @Override
  public List<StatEdge> getPredecessorEdges(int type) {
    return statement.getPredecessorEdges(type);
  }

  @Override
  public List<StatEdge> getAllSuccessorEdges() {
    return statement.getAllSuccessorEdges();
  }

  @Override
  public List<StatEdge> getAllDirectSuccessorEdges() {
    return statement.getAllDirectSuccessorEdges();
  }

  @Override
  public boolean hasAnySuccessor() {
    return statement.hasAnySuccessor();
  }

  @Override
  public boolean hasAnyDirectSuccessor() {
    return statement.hasAnyDirectSuccessor();
  }

  @Override
  public boolean hasSuccessor(int type) {
    return statement.hasSuccessor(type);
  }

  @Override
  public StatEdge getFirstSuccessor() {
    return statement.getFirstSuccessor();
  }

  @Override
  public StatEdge getFirstDirectSuccessor() {
    return statement.getFirstDirectSuccessor();
  }

  @Override
  public List<StatEdge> getAllPredecessorEdges() {
    return statement.getAllPredecessorEdges();
  }

  @Override
  public Statement getFirst() {
    return statement.getFirst();
  }

  @Override
  public void setFirst(Statement first) {
    statement.setFirst(first);
  }

  @Override
  public Statement getPost() {
    return statement.getPost();
  }

  @Override
  public VBStyleCollection<Statement, Integer> getStats() {
    return statement.getStats();
  }

  @Override
  public LastBasicType getLastBasicType() {
    return statement.getLastBasicType();
  }

  @Override
  public HashSet<Statement> getContinueSet() {
    return statement.getContinueSet();
  }

  @Override
  public boolean containsMonitorExit() {
    return statement.containsMonitorExit();
  }

  @Override
  public boolean containsMonitorExitOrAthrow() {
    return statement.containsMonitorExitOrAthrow();
  }

  @Override
  public boolean isMonitorEnter() {
    return statement.isMonitorEnter();
  }

  @Override
  public BasicBlockStatement getBasichead() {
    return statement.getBasichead();
  }

  @Override
  public boolean isLabeled() {
    return statement.isLabeled();
  }

  @Override
  public boolean hasBasicSuccEdge() {
    return statement.hasBasicSuccEdge();
  }

  @Override
  public Statement getParent() {
    return statement.getParent();
  }

  @Override
  public void setParent(Statement parent) {
    statement.setParent(parent);
  }

  @Override
  public RootStatement getTopParent() {
    return statement.getTopParent();
  }

  @Override
  public HashSet<StatEdge> getLabelEdges() {
    return statement.getLabelEdges();
  }

  @Override
  public List<Exprent> getVarDefinitions() {
    return statement.getVarDefinitions();
  }

  @Override
  public List<Exprent> getExprents() {
    return statement.getExprents();
  }

  @Override
  public boolean isCopied() {
    return statement.isCopied();
  }

  @Override
  public void setCopied(boolean copied) {
    statement.setCopied(copied);
  }

  @Override
  public boolean isPhantom() {
    return statement.isPhantom();
  }

  @Override
  public void setPhantom(boolean phantom) {
    statement.setPhantom(phantom);
  }

  @Override
  public String toString() {
    return statement.toString();
  }

  @Override
  public void getOffset(BitSet values) {
    statement.getOffset(values);
  }

  @Override
  public StartEndPair getStartEndRange() {
    return statement.getStartEndRange();
  }

  @Override
  public IMatchable findObject(MatchNode matchNode, int index) {
    return statement.findObject(matchNode, index);
  }

  @Override
  public boolean match(MatchNode matchNode, MatchEngine engine) {
    return statement.match(matchNode, engine);
  }
}
