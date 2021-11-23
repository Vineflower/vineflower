package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.util.DotExporter;

import java.util.*;

public final class TernaryProcessor {
  public static boolean processTernary(RootStatement root) {
    boolean res = false;

    // Quick and dirty postdominator tree
    LinkedList<Statement> toVisit = new LinkedList<>();
    Set<Statement> seen = new HashSet<>();
    for (StatEdge edge : root.getDummyExit().getAllPredecessorEdges()) {
      toVisit.add(edge.getSource());
    }

    List<IfStatement> foundIfs = new ArrayList<>();

    while (!toVisit.isEmpty()) {
      Statement stat = toVisit.removeFirst();
      if (seen.contains(stat)) {
        continue;
      }

      seen.add(stat);

      if (stat.type == Statement.TYPE_IF && ((IfStatement)stat).iftype == IfStatement.IFTYPE_IFELSE) {
        foundIfs.add((IfStatement) stat);
      }

      List<StatEdge> preds = stat.getAllPredecessorEdges();

      // Returns can sometimes have if stats with no predecessors
      if (preds.isEmpty()) {
        Statement parent = stat.getParent();

        if (parent != null) {
          toVisit.add(parent);
        }
      } else {
        for (StatEdge pred : preds) {
          toVisit.add(pred.getSource());
        }
      }
    }

    for (IfStatement ifStat : foundIfs) {
      res |= processIf(ifStat);
    }

    if (res) {
      // TODO: does this even do anything?
      LabelHelper.lowContinueLabels(root, new LinkedHashSet<>());
    }

    return res;
  }

  private static boolean processIf(IfStatement statement) {
    Statement parent = statement.getParent();
    Statement ifStatement = statement.getIfstat();
    Statement elseStatement = statement.getElsestat();

    if (ifStatement.type == Statement.TYPE_IF && elseStatement.type == Statement.TYPE_IF &&
      ifStatement.getExprents() == null && elseStatement.getExprents() == null &&
      ifStatement.getAllSuccessorEdges().size() == 1 && elseStatement.getAllSuccessorEdges().size() == 1 &&
      ifStatement.getAllSuccessorEdges().get(0).getType() == StatEdge.TYPE_BREAK && elseStatement.getAllSuccessorEdges().get(0).getType() == StatEdge.TYPE_BREAK &&
      ifStatement.getAllSuccessorEdges().get(0).getDestination() == elseStatement.getAllSuccessorEdges().get(0).getDestination() &&
      ifStatement.getFirst().getExprents() != null && ifStatement.getFirst().getExprents().isEmpty() &&
      elseStatement.getFirst().getExprents() != null && elseStatement.getFirst().getExprents().isEmpty() &&
      ((IfStatement) ifStatement).getIfstat() == null && ((IfStatement) elseStatement).getIfstat() == null) {

      Statement destination = ifStatement.getAllSuccessorEdges().get(0).getDestination();

      Statement closure = ((IfStatement) ifStatement).getIfEdge().closure;

      // FIXME: Temporary hack to prevent complex ternaries from being emitted
      if (destination.getPredecessorEdges(StatEdge.TYPE_BREAK).size() > 2) {
        return false;
      }

      // Don't create if we have any variable expressions
      if (destination.getExprents() != null) {
        for (Exprent exprent : destination.getExprents()) {
          List<Exprent> exps = exprent.getAllExprents();
          exps.add(exprent);

          for (Exprent expr : exps) {
            if (expr.type == Exprent.EXPRENT_VAR && ((VarExprent)expr).getIndex() >= VarExprent.STACK_BASE) {
              return false;
            }
          }
        }
      }

      // If the closure of the if edge isn't null, make it so the edges to the if statement body won't be inlineable
      if (closure != null && !closure.getAllSuccessorEdges().isEmpty()) {
        for (StatEdge edge : closure.getAllSuccessorEdges().get(0).getDestination().getPredecessorEdges(StatEdge.TYPE_BREAK)) {
          edge.canInline = false;
        }
      }

      StatEdge destEdge = destination.getAllSuccessorEdges().get(0);
      List<Statement> labelsNeedRemoving = new ArrayList<>();
      if (destination.getSuccessorEdges(StatEdge.TYPE_REGULAR).size() == 1) {

        int idx = parent.getStats().getIndexByKey(destination.id);

        int size = parent.getStats().size();

        List<Statement> stats = new ArrayList<>();
        stats.add(destination);
        for (int i = idx + 1; i < size; i++) {
          Statement st = parent.getStats().get(i);

          stats.add(st);
        }

        List<StatEdge> succs = parent.getStats().getLast().getAllSuccessorEdges();
        destEdge = succs.isEmpty() ? null : succs.get(0);

        // fun hack! if we know that we're enclosed in a loop, we can construct a dummy continue edge that flows back into the parent.
        // This fixes stray continue labels in loops that have loops at the end [TestWhileTernary10#test2]
        if (destEdge == null && closure != null && closure.type == Statement.TYPE_DO) {
          destEdge = new StatEdge(StatEdge.TYPE_CONTINUE, statement, closure, closure.getParent());
        }

        labelsNeedRemoving.addAll(stats);
        SequenceStatement seq = new SequenceStatement(stats);

        for (Statement st : stats) {
          parent.getStats().removeWithKey(st.id);
        }

        seq.setParent(parent);
        parent.getStats().addWithKey(seq, seq.id);

        seq.setAllParent();

        destination = seq;
      }

      Exprent condition = statement.getHeadexprent().getCondition();
      Exprent ifCond = ((IfStatement)ifStatement).getHeadexprent().getCondition();
      Exprent elseCond = ((IfStatement)elseStatement).getHeadexprent().getCondition();

      // Need to negate!
      Exprent condA = new FunctionExprent(FunctionExprent.FUNCTION_BOOL_NOT, ifCond, null);
      Exprent condB = new FunctionExprent(FunctionExprent.FUNCTION_BOOL_NOT, elseCond, null);

      // Construct a ternary with the new conditions and set it
      List<Exprent> operands = Arrays.asList(condition, condA, condB);
      statement.getHeadexprent().setCondition(new FunctionExprent(FunctionExprent.FUNCTION_IIF, operands, null));

      // Make if/else to if only
      statement.iftype = IfStatement.IFTYPE_IF;

      BasicBlockStatement bstat1 = SequenceHelper.destroyAndFlattenStatement(ifStatement);
      BasicBlockStatement bstat2 = SequenceHelper.destroyAndFlattenStatement(elseStatement);

      // Destroy basic blocks
      for (StatEdge st : bstat1.getAllSuccessorEdges()) {
        bstat1.removeSuccessor(st);
      }

      for (StatEdge st : bstat2.getAllSuccessorEdges()) {
        bstat2.removeSuccessor(st);
      }

      Statement blockBefore = bstat1.getAllPredecessorEdges().get(0).getSource();

      bstat1.getParent().getStats().removeWithKey(bstat1.id);
      bstat2.getParent().getStats().removeWithKey(bstat2.id);

      for (StatEdge edge : blockBefore.getAllSuccessorEdges()) {
        blockBefore.removeSuccessor(edge);
      }

      // Remove destination predecessors
      // TODO: for complex ternaries we need to make sure all predecessors are accounted for!
      for (StatEdge pred : destination.getAllPredecessorEdges()) {
        pred.getSource().removeSuccessor(pred);
      }

      // Add destination to statement and remove from old parent
      parent.getStats().removeWithKey(destination.id);
      statement.getStats().addWithKey(destination, destination.id);
      destination.setParent(statement);

      // Delete else statement and edge, set destination as the if statement
      statement.setIfstat(destination);
      statement.setElsestat(null);
      statement.setElseEdge(null);

      // Add edge from first statement to if body
      StatEdge edgetoDest = new StatEdge(StatEdge.TYPE_REGULAR, statement.getFirst(), destination);
      // No successors at this point
      statement.getFirst().addSuccessor(edgetoDest);
      statement.setIfEdge(edgetoDest);

      // Get destination edge

      if (destEdge != null) {
        // Update closure
        if (destEdge.closure == parent) {
          destEdge.closure = statement;
        }

        // Add control flow between the if statement and the destination's next statement
        boolean isReturnEdge = destEdge.getDestination().type == Statement.TYPE_DUMMYEXIT;

        List<StatEdge> regEdges = statement.getSuccessorEdges(StatEdge.TYPE_REGULAR);
        for (StatEdge regEdge : regEdges) {
          if (statement.containsStatement(regEdge.getDestination())) {
            statement.removeSuccessor(regEdge);
          }
        }

        if (isReturnEdge) {
          statement.addSuccessor(new StatEdge(StatEdge.TYPE_BREAK, statement, destEdge.getDestination()));
        } else {
          StatEdge edge = new StatEdge(destEdge.getType(), statement, destEdge.getDestination(), destEdge.getDestination().getParent());

          // If the edge from the destination is a continue, modify to break- if keep it as continue then it'll create a double continue and cause problems
          if (destEdge.getType() == StatEdge.TYPE_CONTINUE) {
            edge.setType(StatEdge.TYPE_BREAK);
          }

          statement.addSuccessor(edge);
        }
      }

      for (StatEdge label : new HashSet<>(parent.getLabelEdges())) {
        if (labelsNeedRemoving.contains(label.getDestination())) {
          statement.getParent().getLabelEdges().remove(label);
        }
      }

      return true;
    }

    return false;
  }
}
