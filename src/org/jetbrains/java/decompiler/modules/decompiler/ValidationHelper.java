package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.FlattenStatementsHelper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.DotExporter;
import org.jetbrains.java.decompiler.util.ListStack;
import org.jetbrains.java.decompiler.util.VBStyleCollection;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public final class ValidationHelper {
  private static final boolean VALIDATE = System.getProperty("VALIDATE_DECOMPILED_CODE", "false").equals("true");

  public static void validateStatement(RootStatement statement) {
    if (!VALIDATE) {
      return;
    }

    VBStyleCollection<Statement, Integer> statements = new VBStyleCollection<>();

    Deque<Statement> stack = new LinkedList<>();
    stack.push(statement.getDummyExit());
    stack.push(statement);

    while (!stack.isEmpty()) {
      Statement stat = stack.pop();

      statements.putWithKey(stat, stat.id);

      stack.addAll(stat.getStats());
    }

    for (Statement stat : statements) {
      for (StatEdge edge : stat.getAllSuccessorEdges()) {
        validateEdge(statements, stat, edge);
      }

      for (StatEdge edge : stat.getAllPredecessorEdges()) {
        validateEdge(statements, stat, edge);
      }

      for (StatEdge edge : stat.getLabelEdges()) {
        validateEdge(statements, stat, edge);
      }

      if (stat.getExprents() != null) {
        for (Exprent exprent : stat.getExprents()) {
          for (Exprent ex : exprent.getAllExprents(true, true)) {
            if (ex.type == Exprent.EXPRENT_EXIT) {
              ExitExprent exit = (ExitExprent)ex;
              validateExitExprent(exit);
            }
          }
        }
      }

      if (!statements.contains(stat.getFirst()) && stat.type != Statement.TYPE_DUMMYEXIT && stat.type != Statement.TYPE_BASICBLOCK) {
        throw new IllegalStateException("Non-existing first statement: [" + stat + "] " + stat.getFirst());
      }

      for (Statement statStat : stat.getStats()) {
        if (statStat.getParent() != stat) {
          throw new IllegalStateException("Statement parent is not set correctly: " + statStat);
        }
      }

      if (stat instanceof IfStatement) {
        IfStatement ifstat = (IfStatement)stat;
        validateIfStatement(statements, ifstat);
      }
    }

    FlattenStatementsHelper flatten = new FlattenStatementsHelper();
    DirectGraph directGraph = flatten.buildDirectGraph(statement);
    validateDGraph(directGraph, statement);
  }

  private static void validateEdge(VBStyleCollection<Statement, Integer> statements, Statement stat, StatEdge edge) {
    if (!statements.contains(edge.getSource())) {
      throw new IllegalStateException("Edge pointing from non-existing statement: [" + stat + "] " + edge);
    }

    if (!statements.contains(edge.getDestination())) {
      throw new IllegalStateException("Edge pointing to non-existing statement: [" + stat + "] " + edge);
    }

    if (edge.closure != null) {
      if (!statements.contains(edge.closure)) {
        throw new IllegalStateException("Edge with non-existing closure: [" + stat + "] " + edge);
      }
    }
  }

  private static void validateIfStatement(VBStyleCollection<Statement, Integer> statements, IfStatement ifStat) {
    VBStyleCollection<Statement, Integer> stats = ifStat.getStats();

    if (ifStat.getFirst() == null) {
      throw new IllegalStateException("If statement without a first statement: " + ifStat);
    } else if (!stats.contains(ifStat.getFirst())) {
      throw new IllegalStateException("If statement does not contain own first statement: " + ifStat);
    }

    if (ifStat.getIfEdge() == null) {
      throw new IllegalStateException("If statement without an if edge: " + ifStat);
    }

    if (ifStat.getIfstat() != null) {
      if (ifStat.getIfEdge() != null && ifStat.getIfEdge().getDestination() != ifStat.getIfstat()) {
        throw new IllegalStateException("If statement if edge destination is not ifStat: " + ifStat + " (destination is: " + ifStat.getIfEdge().getDestination() + " but ifStat is: " + ifStat.getIfstat() + ")");
      }

      if (!stats.contains(ifStat.getIfstat())) {
        throw new IllegalStateException("If statement does not contain own ifStat: " + ifStat);
      }

      if (!statements.contains(ifStat.getIfstat())) {
        throw new IllegalStateException("Non-existing ifStat: " + ifStat);
      }
    }

    if (ifStat.iftype == IfStatement.IFTYPE_IF) {
      if (ifStat.getElseEdge() != null) {
        throw new IllegalStateException("If statement with unexpected else edge: " + ifStat);
      }
      if (ifStat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL).isEmpty()){
        throw new IllegalStateException("If statement with no else edge and no successors: " + ifStat);
      } else if (ifStat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL).size() > 1) {
        throw new IllegalStateException("If statement with more than one successor: " + ifStat + " (successors: " + ifStat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL) + ")");
      }
    } else if (ifStat.iftype == IfStatement.IFTYPE_IFELSE) {
      if (ifStat.getElseEdge() == null) {
        throw new IllegalStateException("IfElse statement without else edge: " + ifStat);
      }

      if (ifStat.getElsestat() != null) {
        if (ifStat.getElseEdge() != null && ifStat.getElseEdge().getDestination() != ifStat.getElsestat()) {
          throw new IllegalStateException("IfElse statement else edge destination is not elseStat: " + ifStat);
        }

        if (!stats.contains(ifStat.getElsestat())) {
          throw new IllegalStateException("IfElse statement does not contain own elseStat: " + ifStat);
        }

        if (!statements.contains(ifStat.getElsestat())) {
          throw new IllegalStateException("Non-existing elseStat: " + ifStat);
        }
      }

      // This is a valid case to check, but much of fernflower is built around it so checking it is more trouble than it's worth
      // TODO: fix it properly sometime
//      if (!ifStat.getSuccessorEdges(StatEdge.TYPE_REGULAR).isEmpty()) {
//        throw new IllegalStateException("If-Else statement cannot have regular type successors: [" + ifStat + "] " + ifStat.getSuccessorEdges(StatEdge.TYPE_REGULAR));
//      }
    } else {
      throw new IllegalStateException("Unknown if type: " + ifStat);
    }

    if (ifStat.getIfEdge() != null && ifStat.getIfEdge().getSource() != ifStat.getFirst()) {
      throw new IllegalStateException("If statement if edge source is not first statement: " + ifStat);
    }

    if (ifStat.getElseEdge() != null && ifStat.getElseEdge().getSource() != ifStat.getFirst()) {
      throw new IllegalStateException("IfElse statement else edge source is not first statement: " + ifStat);
    }

    if (stats.size() > 3){
      throw new IllegalStateException("If statement with more than 3 sub statements: " + ifStat);
    }

    for (Statement stat : stats) {
      if ( stat != ifStat.getFirst() && stat != ifStat.getIfstat() && stat != ifStat.getElsestat() ) {
        throw new IllegalStateException("If statement contains unknown sub statement: " + ifStat);
      }
    }

    for (StatEdge edge : ifStat.getFirst().getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL)) {
      if (ifStat.getIfEdge() != edge && ifStat.getElseEdge() != edge) {
        throw new IllegalStateException("If statement first contains unknown successor edge: " + ifStat + " (edge: " + edge + ")");
      }
    }
  }

  public static void validateDGraph(DirectGraph graph, RootStatement root) {
    if (!VALIDATE) {
      return;
    }

    try {
      Set<DirectNode> inaccessibleNodes = new HashSet<>(graph.nodes);

      ListStack<DirectNode> stack = new ListStack<>();
      stack.push(graph.first);
      inaccessibleNodes.remove(graph.first);

      while (!stack.isEmpty()) {
        DirectNode node = stack.pop();

        // check if predecessors have us as a successor
        for (DirectNode pred : node.preds) {
          if (!pred.succs.contains(node)) {
            throw new IllegalStateException("Predecessor " + pred + " does not have " + node + " as a successor");
          }
        }

        // check if successors have us as a predecessor, and remove them from the inaccessible set
        for (DirectNode succ : node.succs) {
          if (!succ.preds.contains(node)) {
            throw new IllegalStateException("Successor " + succ + " does not have " + node + " as a predecessor");
          }

          if (inaccessibleNodes.remove(succ)) {
            // if we find a new accessible node, add it to the stack
            stack.push(succ);
          }
        }
      }

      if (!inaccessibleNodes.isEmpty()) {
        throw new IllegalStateException("Inaccessible nodes: " + inaccessibleNodes);
      }
    } catch (Throwable e){
      DotExporter.errorToDotFile(graph, root.mt, "erroring_dgraph");
      throw e;
    }
  }

  public static void notNull(Object o) {
    if (!VALIDATE) {
      return;
    }

    if (o == null) {
      throw new NullPointerException("Validation: null object: " + o);
    }
  }

  public static void validateExitExprent(ExitExprent exit) {
    if (!VALIDATE) {
      return;
    }

    if (exit.getExitType() == ExitExprent.EXIT_RETURN) {
      if (exit.getRetType().equals(VarType.VARTYPE_VOID)){
        if (exit.getValue() != null) {
          throw new IllegalStateException("Void return with value: " + exit);
        }
      } else {
        if (exit.getValue() == null) {
          throw new IllegalStateException("Non-void return without value: " + exit);
        }
      }
    }
  }
}
