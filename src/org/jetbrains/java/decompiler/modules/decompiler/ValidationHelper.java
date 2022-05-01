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

import java.util.*;

public final class ValidationHelper {
  private static final boolean VALIDATE = System.getProperty("VALIDATE_DECOMPILED_CODE", "false").equals("true");

  public static void validateStatement(RootStatement statement) {
    if (!VALIDATE) {
      return;
    }

    ValidationException ex = null;

    VBStyleCollection<Statement, Integer> statements = new VBStyleCollection<>();

    Deque<Statement> stack = new LinkedList<>();
    stack.push(statement.getDummyExit());
    stack.push(statement);

    while (!stack.isEmpty()) {
      Statement stat = stack.pop();

      statements.putWithKey(stat, stat.id);

      stack.addAll(stat.getStats());

      for (Statement statStat : stat.getStats()) {
        if (statStat.getParent() != stat) {
          ex = error(ex, "Statement parent is not set correctly: " + statStat);
        }
      }
    }

    for (Statement stat : statements) {
      for (StatEdge edge : stat.getAllSuccessorEdges()) {
        ex = validateEdgeContext(statements, stat, edge, ex);
      }

      for (StatEdge edge : stat.getAllPredecessorEdges()) {
        ex = validateEdgeContext(statements, stat, edge, ex);
      }

      for (StatEdge edge : stat.getLabelEdges()) {
        ex = validateEdgeContext(statements, stat, edge, ex);
      }

      if (stat.getExprents() != null) {
        for (Exprent exprent : stat.getExprents()) {
          ex = validateExprentInternal(exprent, ex);
        }
      }

      ex = validateSingleStatementInternal(stat, ex);
    }


    try {
      FlattenStatementsHelper flatten = new FlattenStatementsHelper();
      DirectGraph directGraph = flatten.buildDirectGraph(statement);
      validateDGraph(directGraph, statement);
    } catch (ValidationException e) {
      if (ex == null) {
        ex = e;
      } else {
        ex.messages.addAll(e.messages);
      }
    } catch (Throwable e) {
      ex = error(ex, "Failed to build direct graph: " + e.getMessage());
    }

    check(ex);
  }

  private static ValidationException validateEdgeContext(VBStyleCollection<Statement, Integer> statements, Statement stat, StatEdge edge, ValidationException ex) {
    if (!statements.contains(edge.getSource())) {
      ex = error(ex, "Edge pointing from non-existing statement: [" + stat + "] " + edge);
    }

    if (!statements.contains(edge.getDestination())) {
      ex = error(ex, "Edge pointing to non-existing statement: [" + stat + "] " + edge);
    }

    if (edge.closure != null) {
      if (!statements.contains(edge.closure)) {
        ex = error(ex, "Edge with non-existing closure: [" + stat + "] " + edge);
      }
    }

    return validateEdgeInternal(edge, ex);
  }
  public static void validateEdge(StatEdge edge) {
    if (!VALIDATE) {
      return;
    }

    check(validateEdgeInternal(edge, null));
  }
  private static ValidationException validateEdgeInternal(StatEdge edge, ValidationException ex) {

    if (edge.labeled) {
      if (edge.closure == null) {
        // ex = error(ex, "Edge with label, but no closure: " + edge);
      }
    }

    if (!isSuccessor(edge.getSource(), edge)) {
      ex = error(ex, "Edge pointing from statement but it isn't a successor: " + edge);
    }

    if (!edge.getDestination().getAllPredecessorEdges().contains(edge)) {
      ex = error(ex, "Edge pointing to statement but it isn't a predecessor: " + edge);
    }

    if (edge.labeled && edge.getType() == StatEdge.TYPE_BREAK) {
      if (!edge.getDestination().getLabelEdges().contains(edge)) {
        //ex = error(ex, "Edge with label, but the closure doesn't know: " + edge);
      }
    }

    return ex;
  }

  private static boolean isSuccessor(Statement source, StatEdge edge) {
    if (source.getAllSuccessorEdges().contains(edge)) return true;

    if (source.getParent().type == Statement.TYPE_IF) {
      IfStatement ifstat = (IfStatement) source.getParent();
      if (ifstat.getFirst() == source) {
        if (edge == ifstat.getIfEdge() || edge == ifstat.getElseEdge()) {
          return true;
        }
      }
    }

    return false;
  }

  public static void validateDGraph(DirectGraph graph, RootStatement root) {
    if (!VALIDATE) {
      return;
    }

    ValidationException ex = null;
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
            ex = error(ex, "Predecessor " + pred + " does not have " + node + " as a successor");
          }
        }

        // check if successors have us as a predecessor, and remove them from the inaccessible set
        for (DirectNode succ : node.succs) {
          if (!succ.preds.contains(node)) {
            ex = error(ex, "Successor " + succ + " does not have " + node + " as a predecessor");
          }

          if (inaccessibleNodes.remove(succ)) {
            // if we find a new accessible node, add it to the stack
            stack.push(succ);
          }
        }
      }

      if (!inaccessibleNodes.isEmpty()) {
        ex = error(ex, "Inaccessible nodes: " + inaccessibleNodes);
      }
    } catch (Throwable e){
      ex = error(ex, "Failed to validate graph: " + e.getMessage());
    }

    if (ex != null) {
      DotExporter.errorToDotFile(graph, root.mt, "erroring_dgraph");
      throw ex;
    }
  }

  public static void notNull(Object o) {
    if (!VALIDATE) {
      return;
    }

    if (o == null) {
      throw new ValidationException("Validation: null object");
    }
  }

  public static void validateSingleStatement(Statement stat) {
    if (!VALIDATE) {
      return;
    }

    check(validateSingleStatementInternal(stat, null));
  }

  // non recursive
  private static ValidationException validateSingleStatementInternal(Statement stat, ValidationException ex) {

    switch (stat.type) {
      case Statement.TYPE_IF: ex = validateIfStatementInternal((IfStatement) stat, ex); break;
    }

    return ex;
  }

  public static void validateIfStatement(IfStatement ifStat) {
    if (!VALIDATE) {
      return;
    }
    
    check(validateIfStatementInternal(ifStat, null));
  }
  private static ValidationException validateIfStatementInternal(IfStatement ifStat, ValidationException ex) {

    final VBStyleCollection<Statement, Integer> stats = ifStat.getStats();

    if (ifStat.getFirst() == null) {
      ex = error(ex, "If statement without a first statement: " + ifStat);
    } else if (!stats.contains(ifStat.getFirst())) {
      ex = error(ex, "If statement does not contain own first statement: " + ifStat);
    }

    if (ifStat.getIfEdge() == null) {
      ex = error(ex, "If statement without an if edge: " + ifStat);
    }

    if (ifStat.getIfstat() != null) {
      if (ifStat.getIfEdge() != null && ifStat.getIfEdge().getDestination() != ifStat.getIfstat()) {
        ex = error(ex, "If statement if edge destination is not ifStat: " + ifStat + " (destination is: " + ifStat.getIfEdge().getDestination() + " but ifStat is:" + ifStat.getIfstat() + ")");
      }

      if (!stats.contains(ifStat.getIfstat())) {
        ex = error(ex, "If statement does not contain own ifStat: " + ifStat);
      }
    }

    if (ifStat.iftype == IfStatement.IFTYPE_IF) {
      if (ifStat.getElseEdge() != null) {
        ex = error(ex, "If statement with unexpected else edge: " + ifStat);
      }
      if (ifStat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL).isEmpty()){
        ex = error(ex, "If statement with no else edge and no successors: " + ifStat);
      } else if (ifStat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL).size() > 1) {
        ex = error(ex, "If statement with more than one successor: " + ifStat + " (successors: " + ifStat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL) + ")");
      }
    } else if (ifStat.iftype == IfStatement.IFTYPE_IFELSE) {
      if (ifStat.getElseEdge() == null) {
        ex = error(ex, "IfElse statement without else edge: " + ifStat);
      }

      if (ifStat.getElsestat() != null) {
        if (ifStat.getElseEdge() != null && ifStat.getElseEdge().getDestination() != ifStat.getElsestat()) {
          ex = error(ex, "IfElse statement else edge destination is not elseStat: " + ifStat);
        }

        if (!stats.contains(ifStat.getElsestat())) {
          ex = error(ex, "IfElse statement does not contain own elseStat: " + ifStat);
        }
      }
    } else {
      ex = error(ex, "Unknown if type: " + ifStat);
    }

    if (ifStat.getIfEdge() != null && ifStat.getIfEdge().getSource() != ifStat.getFirst()) {
      ex = error(ex, "If statement if edge source is not first statement: " + ifStat);
    }

    if (ifStat.getElseEdge() != null && ifStat.getElseEdge().getSource() != ifStat.getFirst()) {
      ex = error(ex, "IfElse statement else edge source is not first statement: " + ifStat);
    }

    if (stats.size() > 3){
      ex = error(ex, "If statement with more than 3 sub statements: " + ifStat);
    }

    for (Statement stat : stats) {
      if ( stat != ifStat.getFirst() && stat != ifStat.getIfstat() && stat != ifStat.getElsestat() ) {
        ex = error(ex, "If statement contains unknown sub statement: " + ifStat);
      }
    }

    for (StatEdge edge : ifStat.getFirst().getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL)) {
      if (ifStat.getIfEdge() != edge && ifStat.getElseEdge() != edge) {
        ex = error(ex, "If statement contains unknown successor edge: " + ifStat + " (edge: " + edge + ")");
      }
    }

    return ex;
  }

  public static void validateExprent(Exprent exprent) {
    if (!VALIDATE) {
      return;
    }
    
    check(validateExprentInternal(exprent, null));
  }
  private static ValidationException validateExprentInternal(Exprent exprent, ValidationException ex){

    switch (exprent.type) {
      case Exprent.EXPRENT_EXIT: ex = validateExitExprentInternal((ExitExprent)exprent, ex); break;
      default: {
        for (Exprent subExprents : exprent.getAllExprents()) {
          ex = validateExprentInternal(subExprents, ex);
        }
      }
    }
    
    return ex;
  }
  public static void validateExitExprent(ExitExprent exit) {
    if (!VALIDATE) {
      return;
    }
    
    check(validateExitExprentInternal(exit, null));
  }

  public static ValidationException validateExitExprentInternal(ExitExprent exit, ValidationException ex){
    if (exit.getExitType() == ExitExprent.EXIT_RETURN) {
      if (exit.getRetType().equals(VarType.VARTYPE_VOID)){
        if (exit.getValue() != null) {
          ex = error(ex, "Void return with value: " + exit);
        }
      } else {
        if (exit.getValue() == null) {
          ex = error(ex, "Non-void return without value: " + exit);
        }
      }
    }

    for (Exprent subExprents : exit.getAllExprents()) {
      ex = validateExprentInternal(subExprents, ex);
    }
    
    return ex;
  }

  public static void singleSuccessor(Statement stat) {
    if (!VALIDATE) {
      return;
    }

    if (stat.getAllSuccessorEdges().size() != 1) {
      throw new ValidationException("Statement has more than one successor: " + stat);
    }
  }
  
  private static ValidationException error(ValidationException prev, String msg) {
    if (prev == null) {
      return new ValidationException(msg);
    } else {
      prev.messages.add(msg);
      return prev;
    }
  }
  
  private static void check(ValidationException error){
    if (error != null) {
      throw error;
    }
  }
  
  public static class ValidationException extends RuntimeException {
    public List<String> messages = new ArrayList<String>();
    public ValidationException(String message) {
      super();
      this.messages.add(message);
    }
    
    public ValidationException(String message, Throwable cause) {
      super(cause);
      this.messages.add(message);
    }

    @Override
    public String getMessage() {
      StringJoiner joiner = new StringJoiner("\n");
      for (String msg : this.messages) {
        joiner.add(msg);
      }
      return joiner.toString();
    }
  }
}
