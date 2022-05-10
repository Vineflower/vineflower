// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ArrayExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.IfExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.modules.decompiler.vars.CheckTypesResult;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.DotExporter;
import org.jetbrains.java.decompiler.util.VBStyleCollection;

import java.util.*;

public final class MergeHelper {
  public static void enhanceLoops(Statement root) {
    while (enhanceLoopsRec(root)) /**/;
    SequenceHelper.condenseSequences(root);
  }

  private static boolean enhanceLoopsRec(Statement stat) {
    boolean res = false;

    for (Statement st : new ArrayList<>(stat.getStats())) {
      if (st.getExprents() == null) {
        res |= enhanceLoopsRec(st);
      }
    }

    if (stat.type == Statement.TYPE_DO) {
      res |= enhanceLoop((DoStatement)stat);
    }

    return res;
  }

  private static boolean enhanceLoop(DoStatement stat) {
    int oldloop = stat.getLooptype();

    switch (oldloop) {
      case DoStatement.LOOP_DO:

        // identify a while loop
        if (matchWhile(stat)) {
          if (!matchForEach(stat)) {
            matchFor(stat);
          }
        }
        else {
          // identify a do{}while loop
          //matchDoWhile(stat);
        }

        break;
      case DoStatement.LOOP_WHILE:
        if (!matchForEach(stat)) {
          matchFor(stat);
        }
    }

    return (stat.getLooptype() != oldloop);
  }

  private static void matchDoWhile(DoStatement stat) {
    // search for an if condition at the end of the loop
    Statement last = stat.getFirst();
    while (last.type == Statement.TYPE_SEQUENCE) {
      last = last.getStats().getLast();
    }

    if (last.type == Statement.TYPE_IF) {
      IfStatement lastif = (IfStatement)last;
      if (lastif.iftype == IfStatement.IFTYPE_IF && lastif.getIfstat() == null) {
        StatEdge ifedge = lastif.getIfEdge();
        StatEdge elseedge = lastif.getFirstSuccessor();

        if ((ifedge.getType() == StatEdge.TYPE_BREAK && elseedge.getType() == StatEdge.TYPE_CONTINUE && elseedge.closure == stat
             && isDirectPath(stat, ifedge.getDestination())) ||
            (ifedge.getType() == StatEdge.TYPE_CONTINUE && elseedge.getType() == StatEdge.TYPE_BREAK && ifedge.closure == stat
            && isDirectPath(stat, elseedge.getDestination())) ||
          // Break labeled as continue case
          (ifedge.getType() == StatEdge.TYPE_CONTINUE && elseedge.getType() == StatEdge.TYPE_CONTINUE && elseedge.closure == stat
            && isLastInLoop(stat) && isDirectPath(stat, ifedge.getDestination()))
        ) {

          Set<Statement> set = stat.getNeighboursSet(StatEdge.TYPE_CONTINUE, Statement.DIRECTION_BACKWARD);
          set.remove(last);

          if (!set.isEmpty()) {
            return;
          }

          stat.setLooptype(DoStatement.LOOP_DOWHILE);

          IfExprent ifexpr = (IfExprent)lastif.getHeadexprent().copy();
          if (ifedge.getType() == StatEdge.TYPE_BREAK) {
            ifexpr.negateIf();
          }

          if (stat.getConditionExprent() != null) {
            ifexpr.getCondition().addBytecodeOffsets(stat.getConditionExprent().bytecode);
          }
          ifexpr.getCondition().addBytecodeOffsets(lastif.getHeadexprent().bytecode);

          stat.setConditionExprent(ifexpr.getCondition());
          lastif.removeSuccessor(elseedge);

          // remove empty if
          if (lastif.getFirst().getExprents().isEmpty()) {
            // Remove reference to the if statement
            lastif.getFirst().removeSuccessor(ifedge);

            removeLastEmptyStatement(stat, lastif);
          } else if (stat.getStats().size() == 1 && ifedge.getType() == StatEdge.TYPE_CONTINUE) { // Only statement in loop is if
            // Otherwise leaves behind a superfluous if statement which is then removed by SequenceHelper
            // Only when ifedge is type continue, or else break edge is kept

            lastif.replaceWith(lastif.getFirst());
          } else {
            lastif.setExprents(lastif.getFirst().getExprents());

            StatEdge newedge = new StatEdge(StatEdge.TYPE_CONTINUE, lastif, stat);
            lastif.addSuccessor(newedge);
            stat.addLabeledEdge(newedge);
          }

          if (stat.getAllSuccessorEdges().isEmpty()) {
            StatEdge edge = elseedge.getType() == StatEdge.TYPE_CONTINUE ? ifedge : elseedge;

            edge.setSource(stat);
            if (edge.closure == stat) {
              edge.closure = stat.getParent();
            }
            stat.addSuccessor(edge);
          }
        }
      }
    }
  }

  private static boolean isLastInLoop(Statement stat) {
    while (stat.getParent().type == Statement.TYPE_SEQUENCE) {
      if (stat.getParent().getStats().indexOf(stat) != stat.getParent().getStats().size() - 1) {
        return false;
      }

      stat = stat.getParent();

      if (stat == null) {
        return false;
      }
    }

    return stat.getParent().type == Statement.TYPE_DO;
  }

  private static boolean matchWhile(DoStatement stat) {

    // search for an if condition at the entrance of the loop
    Statement first = stat.getFirst();
    while (first.type == Statement.TYPE_SEQUENCE) {
      first = first.getFirst();
    }

    // found an if statement
    if (first.type == Statement.TYPE_IF) {
      IfStatement firstif = (IfStatement)first;

      if (firstif.getFirst().getExprents().isEmpty()) {

        if (firstif.iftype == IfStatement.IFTYPE_IF) {
          if (firstif.getIfstat() == null) {
            StatEdge ifedge = firstif.getIfEdge();

            if (isDirectPath(stat, ifedge.getDestination()) || addContinueOrBreak(stat, ifedge)) {
              // exit condition identified
              stat.setLooptype(DoStatement.LOOP_WHILE);

              // negate condition (while header)
              IfExprent ifexpr = (IfExprent)firstif.getHeadexprent().copy();
                ifexpr.negateIf();

              if (stat.getConditionExprent() != null) {
                ifexpr.getCondition().addBytecodeOffsets(stat.getConditionExprent().bytecode);
              }
              ifexpr.getCondition().addBytecodeOffsets(firstif.getHeadexprent().bytecode);

              stat.setConditionExprent(ifexpr.getCondition());

              // remove edges
              firstif.getFirst().removeSuccessor(ifedge);
              firstif.removeSuccessor(firstif.getAllSuccessorEdges().get(0));

              if (stat.getAllSuccessorEdges().isEmpty()) {
                ifedge.setSource(stat);
                if (ifedge.closure == stat) {
                  ifedge.closure = stat.getParent();
                }
                stat.addSuccessor(ifedge);
              }

              // remove empty if statement as it is now part of the loop
              if (firstif == stat.getFirst()) {
                BasicBlockStatement bstat = new BasicBlockStatement(new BasicBlock(
                  DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.STATEMENT_COUNTER)));
                bstat.setExprents(new ArrayList<>());
                stat.replaceStatement(firstif, bstat);
              }
              else {
                // precondition: sequence must contain more than one statement!
                Statement sequence = firstif.getParent();
                sequence.getStats().removeWithKey(firstif.id);

                if (!sequence.getStats().isEmpty()) {
                  sequence.setFirst(sequence.getStats().get(0));
                } else {
                  SequenceHelper.destroyAndFlattenStatement(sequence);
                }
              }

              return true;
            }
          }
          //else { // fix infinite loops

          StatEdge elseEdge = firstif.getFirstSuccessor();
          if (isDirectPath(stat, elseEdge.getDestination())) {
            // FIXME: This is horrible and bad!! Needs an extraction step before loop merging!!
            if (isIif(firstif.getHeadexprent().getCondition())) {
              return false;
            }

            // exit condition identified
            stat.setLooptype(DoStatement.LOOP_WHILE);

            // no need to negate the while condition
            IfExprent ifexpr = (IfExprent)firstif.getHeadexprent().copy();
            if (stat.getConditionExprent() != null) {
              ifexpr.getCondition().addBytecodeOffsets(stat.getConditionExprent().bytecode);
            }
            ifexpr.getCondition().addBytecodeOffsets(firstif.getHeadexprent().bytecode);
            stat.setConditionExprent(ifexpr.getCondition());

            // remove edges
            StatEdge ifedge = firstif.getIfEdge();
            firstif.getFirst().removeSuccessor(ifedge);
            firstif.removeSuccessor(elseEdge);

            if (stat.getAllSuccessorEdges().isEmpty()) {
              elseEdge.setSource(stat);
              if (elseEdge.closure == stat) {
                elseEdge.closure = stat.getParent();
              }
              stat.addSuccessor(elseEdge);
            }

            if (firstif.getIfstat() == null) {
              BasicBlockStatement bstat = new BasicBlockStatement(new BasicBlock(
                DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.STATEMENT_COUNTER)));
              bstat.setExprents(new ArrayList<>());

              ifedge.setSource(bstat);
              bstat.addSuccessor(ifedge);

              // TODO: has the potential of breaking if firstif isn't found due to our changes
              stat.replaceStatement(firstif, bstat);
            }
            else {
              // replace the if statement with its content
              first.getParent().replaceStatement(first, firstif.getIfstat());

              // lift closures
              for (StatEdge prededge : elseEdge.getDestination().getPredecessorEdges(StatEdge.TYPE_BREAK)) {

                if (stat.containsStatementStrict(prededge.closure)) {
                  stat.addLabeledEdge(prededge);
                }
              }

              LabelHelper.lowClosures(stat);
            }

            return true;
          }
        }
      }
    }

    return false;
  }

  private static boolean isIif(Exprent exprent) {
    if (exprent.type != Exprent.EXPRENT_FUNCTION) {
      return false;
    }

    Exprent check = exprent;
    while (check.type == Exprent.EXPRENT_FUNCTION && ((FunctionExprent)check).getFuncType() == FunctionExprent.FUNCTION_BOOL_NOT) {
      check = ((FunctionExprent)check).getLstOperands().get(0);
    }

    return check.type == Exprent.EXPRENT_FUNCTION && ((FunctionExprent)check).getFuncType() == FunctionExprent.FUNCTION_IIF;
  }

  private static void liftToParent(DoStatement stat, List<Statement> toAdd) {
    VBStyleCollection<Statement, Integer> stats = stat.getParent().getStats();
    if (stat.getParent().type == Statement.TYPE_SEQUENCE) {
      int i = 0;

      for (Statement st : toAdd) {
        i++;
        // Add sequentially after while loop
        stats.addWithKeyAndIndex(stats.indexOf(stat) + i, st, st.id);
      }

      stat.getParent().setAllParent();
    } else {
      // If it's not part of a sequence statement, we need to synthesize one and replace the loop with it

      // Add while loop to the beginning of the new sequence
      toAdd.add(0, stat);

      // Old index of the while loop
      int idx = stats.getIndexByKey(stat.id);
      // Remove while loop from it's parent
      stats.removeWithKey(stat.id);

      Statement par = stat.getParent();
      // If the parent's first statement points towards the while loop, we need to update it
      boolean replaceFirst = par.getFirst() == stat;

      // Construct new sequence out of the while loop and it's non loop content
      SequenceStatement seq = new SequenceStatement(toAdd);
      // Set parent of sequence to be the while loop's parent
      seq.setParent(par);
      // Set parent to it's children
      seq.setAllParent();
      // Add to the while loop's parent
      stats.addWithKeyAndIndex(idx, seq, seq.id);

      if (replaceFirst) {
        // Update first statement of parent
        par.setFirst(seq);
      }
    }
  }

  // Returns if the statement provided and the end statement provided has a direct control flow path
  public static boolean isDirectPath(Statement stat, Statement endstat) {
    Set<Statement> forwardEdges = stat.getNeighboursSet(Statement.STATEDGE_DIRECT_ALL, Statement.DIRECTION_FORWARD);

    if (forwardEdges.isEmpty()) {
      Statement parent = stat.getParent();

      if (parent == null) {
        return endstat.type == Statement.TYPE_DUMMYEXIT;
      } else {
        switch (parent.type) {
          case Statement.TYPE_ROOT:
            return endstat.type == Statement.TYPE_DUMMYEXIT;
          case Statement.TYPE_DO:
            return endstat == parent;
          case Statement.TYPE_SWITCH:
            SwitchStatement swst = (SwitchStatement)parent;

            for (int i = 0; i < swst.getCaseStatements().size() - 1; i++) {
              Statement caseStatement = swst.getCaseStatements().get(i);

              if (caseStatement == stat) {
                Statement nextCase = swst.getCaseStatements().get(i + 1);

                if (nextCase.getExprents() != null && nextCase.getExprents().isEmpty()) {
                  nextCase = nextCase.getAllSuccessorEdges().get(0).getDestination();
                }

                return endstat == nextCase;
              }
            }
          default:
            return isDirectPath(parent, endstat);
        }
      }
    } else {
      return forwardEdges.contains(endstat);
    }
  }

  private static void matchFor(DoStatement stat) {
    Exprent lastDoExprent, initDoExprent;
    Statement lastData, preData = null;

    // get last exprent
    lastData = getLastDirectData(stat.getFirst());
    if (lastData == null || lastData.getExprents().isEmpty()) {
      return;
    }

    List<Exprent> lstExpr = lastData.getExprents();
    lastDoExprent = lstExpr.get(lstExpr.size() - 1);

    boolean issingle = false;
    if (lstExpr.size() == 1) {  // single exprent
      if (lastData.getAllPredecessorEdges().size() > 1) { // break edges
        issingle = true;
      }
    }

    boolean haslast = issingle || lastDoExprent.type == Exprent.EXPRENT_ASSIGNMENT || lastDoExprent.type == Exprent.EXPRENT_FUNCTION;
    if (!haslast) {
      return;
    }

    boolean hasinit = false;

    // search for an initializing exprent
    Statement current = stat;
    while (true) {
      Statement parent = current.getParent();
      if (parent == null) {
        break;
      }

      if (parent.type == Statement.TYPE_SEQUENCE) {
        if (current == parent.getFirst()) {
          current = parent;
        }
        else {
          preData = current.getNeighbours(StatEdge.TYPE_REGULAR, Statement.DIRECTION_BACKWARD).get(0);
          // we're not a basic block, so we can't dive inside for exprents
          if (preData.type != Statement.TYPE_BASICBLOCK) break;
          preData = getLastDirectData(preData);
          if (preData != null && !preData.getExprents().isEmpty()) {
            initDoExprent = preData.getExprents().get(preData.getExprents().size() - 1);
            if (initDoExprent.type == Exprent.EXPRENT_ASSIGNMENT) {
              hasinit = true;
            }
          }
          break;
        }
      }
      else {
        break;
      }
    }

    if (hasinit || issingle) {  // FIXME: issingle sufficient?
      Set<Statement> set = stat.getNeighboursSet(StatEdge.TYPE_CONTINUE, Statement.DIRECTION_BACKWARD);
      set.remove(lastData);

      if (!set.isEmpty()) {
        return;
      }

      // We don't want to make for loops that have an empty body.
      // The nature of the for loop filter makes it so any loop that contains the pattern "1) assignment 2) loop 3) assignment" will become a for loop.
      // Break out of the loop if we find that the third assignment is the only expression in the basic block.

      // First filter to make sure that the loop body (includes the last assignment) has a single statement.

      Statement firstStat = stat.getFirst();
      while (firstStat.type == Statement.TYPE_SEQUENCE && firstStat.getStats().size() == 1) {
        Statement fst = firstStat.getFirst();
        if (fst == null) {
          break;
        }

        firstStat = fst;
      }

      // Then filter to make sure the loop body is a basic block (Seems like it always is- but it never hurts to double check!)
      if (firstStat.type == Statement.TYPE_BASICBLOCK) {

        // Last filter to make sure the basic block only has the final third assignment. If so, break out to make a while loop instead as it will produce cleaner output.
        if (firstStat.getExprents().size() == 1) {
          return;
        }
      }

      stat.setLooptype(DoStatement.LOOP_FOR);
      if (hasinit) {
        Exprent exp = preData.getExprents().remove(preData.getExprents().size() - 1);
        if (stat.getInitExprent() != null) {
          exp.addBytecodeOffsets(stat.getInitExprent().bytecode);
        }
        stat.setInitExprent(exp);
      }
      Exprent exp = lastData.getExprents().remove(lastData.getExprents().size() - 1);
      if (stat.getIncExprent() != null) {
        exp.addBytecodeOffsets(stat.getIncExprent().bytecode);
      }
      stat.setIncExprent(exp);
    }

    cleanEmptyStatements(stat, lastData);
  }

  private static void cleanEmptyStatements(DoStatement dostat, Statement stat) {
    if (stat != null && stat.getExprents().isEmpty()) {
      List<StatEdge> lst = stat.getAllSuccessorEdges();
      if (!lst.isEmpty()) {
        stat.removeSuccessor(lst.get(0));
      }

      // Cannot delete try or catch blocks with finally
      if (stat.getParent().type == Statement.TYPE_TRYCATCH || stat.getParent().type == Statement.TYPE_CATCHALL) {
        return;
      }

      removeLastEmptyStatement(dostat, stat);
    }
  }

  private static void removeLastEmptyStatement(DoStatement dostat, Statement stat) {

    if (stat == dostat.getFirst()) {
      BasicBlockStatement bstat = BasicBlockStatement.create();
      dostat.replaceStatement(stat, bstat);
    } else {
      for (StatEdge edge : stat.getAllPredecessorEdges()) {
        // Change edge type to continue
        edge.getSource().changeEdgeType(Statement.DIRECTION_FORWARD, edge, StatEdge.TYPE_CONTINUE);

        // Remove edge from old destination
        stat.removePredecessor(edge);

        // Change destination to enclosing loop
        edge.getSource().changeEdgeNode(Statement.DIRECTION_FORWARD, edge, dostat);
        // Make sure enclosing loop knows about the new edge
        dostat.addPredecessor(edge);

        // Set that edge as labeled
        dostat.addLabeledEdge(edge);
      }

      // parent is a sequence statement
      stat.getParent().getStats().removeWithKey(stat.id);

      // Quiltflower note: Parent isn't always a sequence statement! It can be an if statement, need to check for that case! [TestLoopFinally]
      if (stat.getParent().type == Statement.TYPE_IF) {
        IfStatement parent = (IfStatement)stat.getParent();

        // Replace owning stats
        if (parent.getIfstat() == stat) {
          parent.setIfstat(null);
        } else if (parent.getElsestat() == stat) {
          parent.setElsestat(null);
        }
      }

      // TODO: switch statements?
    }
  }

  private static Statement getLastDirectData(Statement stat) {
    if (stat.getExprents() != null) {
      return stat;
    }

    for (int i = stat.getStats().size() - 1; i >= 0; i--) {
      Statement tmp = getLastDirectData(stat.getStats().get(i));
      if (tmp == null || !tmp.getExprents().isEmpty()) {
        return tmp;
      }
    }
    return null;
  }

  private static boolean matchForEach(DoStatement stat) {
    AssignmentExprent firstDoExprent = null;
    AssignmentExprent[] initExprents = new AssignmentExprent[3];
    Statement firstData = null, preData = null, lastData = null;
    Exprent lastExprent = null;

    // search for an initializing exprent
    Statement current = stat;
    while (true) {
      Statement parent = current.getParent();
      if (parent == null) {
        break;
      }

      if (parent.type == Statement.TYPE_SEQUENCE) {
        if (current == parent.getFirst()) {
          current = parent;
        }
        else {
          preData = current.getNeighbours(StatEdge.TYPE_REGULAR, Statement.DIRECTION_BACKWARD).get(0);
          preData = getLastDirectData(preData);
          if (preData != null && !preData.getExprents().isEmpty()) {
            int size = preData.getExprents().size();
            for (int x = 0; x < initExprents.length; x++) {
              if (size > x) {
                 Exprent exprent = preData.getExprents().get(size - 1 - x);
                 if (exprent.type == Exprent.EXPRENT_ASSIGNMENT) {
                   initExprents[x] = (AssignmentExprent)exprent;
                 }
              }
            }
          }
          break;
        }
      }
      else {
        break;
      }
    }

    firstData = getFirstDirectData(stat.getFirst());
    if (firstData != null && firstData.getExprents().get(0).type == Exprent.EXPRENT_ASSIGNMENT) {
      firstDoExprent = (AssignmentExprent)firstData.getExprents().get(0);
    }
    lastData = getLastDirectData(stat.getFirst());
    if (lastData != null && !lastData.getExprents().isEmpty()) {
      lastExprent = lastData.getExprents().get(lastData.getExprents().size() - 1);
    }

    if (stat.getLooptype() == DoStatement.LOOP_WHILE && initExprents[0] != null && firstDoExprent != null) {
      if (isIteratorCall(initExprents[0].getRight())) {

        //Streams mimic Iterable but arnt.. so explicitly disallow their enhancements
        //TODO: Check inheritance for Iterable instead of just names?
        InvocationExprent invc = (InvocationExprent)getUncast((initExprents[0]).getRight());
        if (invc.getClassname().contains("java/util/stream")) {
          return false;
        }

        if (!isHasNextCall(drillNots(stat.getConditionExprent())) ||
            firstDoExprent.type != Exprent.EXPRENT_ASSIGNMENT) {
          return false;
        }

        AssignmentExprent ass = firstDoExprent;
        if ((!isNextCall(ass.getRight()) && !isNextUnboxing(ass.getRight())) || ass.getLeft().type != Exprent.EXPRENT_VAR) {
          return false;
        }

        InvocationExprent next = (InvocationExprent)getUncast(ass.getRight());
        if (isNextUnboxing(next))
          next = (InvocationExprent)getUncast(next.getInstance());
        InvocationExprent hnext = (InvocationExprent)getUncast(drillNots(stat.getConditionExprent()));
        if (next.getInstance().type != Exprent.EXPRENT_VAR ||
            hnext.getInstance().type != Exprent.EXPRENT_VAR ||
          ((VarExprent)initExprents[0].getLeft()).isVarReferenced(stat, (VarExprent)next.getInstance(), (VarExprent)hnext.getInstance())) {
          return false;
        }

        // Casted foreach
        Exprent right = initExprents[0].getRight();
        if (right.type == Exprent.EXPRENT_FUNCTION) {
          FunctionExprent fRight = (FunctionExprent) right;
          if (fRight.getFuncType() == FunctionExprent.FUNCTION_CAST) {
            right = fRight.getLstOperands().get(0);
          }

          if (right.type == Exprent.EXPRENT_INVOCATION) {
            return false;
          }
        }
        InvocationExprent holder = (InvocationExprent)right;

        initExprents[0].getBytecodeRange(holder.getInstance().bytecode);
        holder.getBytecodeRange(holder.getInstance().bytecode);
        firstDoExprent.getBytecodeRange(ass.getLeft().bytecode);
        ass.getRight().getBytecodeRange(ass.getLeft().bytecode);
        if (stat.getIncExprent() != null) {
          stat.getIncExprent().getBytecodeRange(holder.getInstance().bytecode);
        }
        if (stat.getInitExprent() != null) {
          stat.getInitExprent().getBytecodeRange(ass.getLeft().bytecode);
        }

        stat.setLooptype(DoStatement.LOOP_FOREACH);
        stat.setInitExprent(ass.getLeft());
        stat.setIncExprent(holder.getInstance());
        preData.getExprents().remove(initExprents[0]);
        firstData.getExprents().remove(firstDoExprent);

        if (initExprents[1] != null && initExprents[1].getLeft().type == Exprent.EXPRENT_VAR &&
            holder.getInstance().type == Exprent.EXPRENT_VAR) {
          VarExprent copy = (VarExprent)initExprents[1].getLeft();
          VarExprent inc = (VarExprent)holder.getInstance();
          if (copy.getIndex() == inc.getIndex() && copy.getVersion() == inc.getVersion() &&
              !inc.isVarReferenced(stat.getTopParent(), copy) && !isNextCall(initExprents[1].getRight())) {
            preData.getExprents().remove(initExprents[1]);
            initExprents[1].getBytecodeRange(initExprents[1].getRight().bytecode);
            stat.getIncExprent().getBytecodeRange(initExprents[1].getRight().bytecode);
            stat.setIncExprent(initExprents[1].getRight());
          }
        }

        // Type of assignment- store in var for type calculation
        CheckTypesResult typeRes = ass.checkExprTypeBounds();
        if (typeRes != null && !typeRes.getLstMinTypeExprents().isEmpty()) {
          VarType boundType = typeRes.getLstMinTypeExprents().get(0).type;
          VarExprent var = (VarExprent) ass.getLeft();
          var.setBoundType(boundType);
        }

        return true;
      } else if (initExprents[1] != null) {
        if (firstDoExprent.getRight().type != Exprent.EXPRENT_ARRAY || firstDoExprent.getLeft().type != Exprent.EXPRENT_VAR) {
          return false;
        }

        if (lastExprent == null || lastExprent.type != Exprent.EXPRENT_FUNCTION) {
          return false;
        }

        if (initExprents[0].getRight().type != Exprent.EXPRENT_CONST ||
            initExprents[1].getRight().type != Exprent.EXPRENT_FUNCTION ||
            stat.getConditionExprent().type != Exprent.EXPRENT_FUNCTION) {
          return false;
        }

        //FunctionExprent funcCond  = (FunctionExprent)drillNots(stat.getConditionExprent()); //TODO: Verify this is counter < copy.length
        FunctionExprent funcRight = (FunctionExprent)initExprents[1].getRight();
        FunctionExprent funcInc   = (FunctionExprent)lastExprent;
        ArrayExprent    arr       = (ArrayExprent)firstDoExprent.getRight();
        int incType = funcInc.getFuncType();

        if (funcRight.getFuncType() != FunctionExprent.FUNCTION_ARRAY_LENGTH ||
            (incType != FunctionExprent.FUNCTION_PPI && incType != FunctionExprent.FUNCTION_IPP) ||
            arr.getIndex().type != Exprent.EXPRENT_VAR ||
            arr.getArray().type != Exprent.EXPRENT_VAR) {
            return false;
        }

        VarExprent index = (VarExprent)arr.getIndex();
        VarExprent array = (VarExprent)arr.getArray();
        VarExprent counter = (VarExprent)funcInc.getLstOperands().get(0);

        if (counter.getIndex() != index.getIndex() ||
            counter.getVersion() != index.getVersion()) {
          return false;
        }

        if (counter.isVarReferenced(stat.getFirst(), index)) {
          return false;
        }

        // Add bytecode offsets
        funcRight.getLstOperands().get(0).addBytecodeOffsets(initExprents[0].bytecode);
        funcRight.getLstOperands().get(0).addBytecodeOffsets(initExprents[1].bytecode);
        funcRight.getLstOperands().get(0).addBytecodeOffsets(lastExprent.bytecode);
        firstDoExprent.getLeft().addBytecodeOffsets(firstDoExprent.bytecode);
        firstDoExprent.getLeft().addBytecodeOffsets(initExprents[0].bytecode);

        stat.setLooptype(DoStatement.LOOP_FOREACH);
        stat.setInitExprent(firstDoExprent.getLeft());
        stat.setIncExprent(funcRight.getLstOperands().get(0));
        preData.getExprents().remove(initExprents[0]);
        preData.getExprents().remove(initExprents[1]);
        firstData.getExprents().remove(firstDoExprent);
        lastData.getExprents().remove(lastExprent);

        if (initExprents[2] != null && initExprents[2].getLeft().type == Exprent.EXPRENT_VAR) {
          VarExprent copy = (VarExprent)initExprents[2].getLeft();

          if (copy.getIndex() == array.getIndex() && copy.getVersion() == array.getVersion()) {
            preData.getExprents().remove(initExprents[2]);
            initExprents[2].getRight().addBytecodeOffsets(initExprents[2].bytecode);
            initExprents[2].getRight().addBytecodeOffsets(stat.getIncExprent().bytecode);
            stat.setIncExprent(initExprents[2].getRight());
          }
        }

        // Type of assignment- store in var for type calculation
        CheckTypesResult typeRes = firstDoExprent.checkExprTypeBounds();
        if (typeRes != null && !typeRes.getLstMinTypeExprents().isEmpty()) {
          VarType boundType = typeRes.getLstMinTypeExprents().get(0).type;
          VarExprent var = (VarExprent) firstDoExprent.getLeft();
          var.setBoundType(boundType);
        }

        return true;
      }
    }

    //cleanEmptyStatements(stat, firstData); //TODO: Look into this and see what it does...

    return false;
  }

  private static Exprent drillNots(Exprent exp) {
    while (true) {
      if (exp.type == Exprent.EXPRENT_FUNCTION) {
        FunctionExprent fun = (FunctionExprent)exp;
        if (fun.getFuncType() == FunctionExprent.FUNCTION_BOOL_NOT) {
          exp = fun.getLstOperands().get(0);
        }
        else if (fun.getFuncType() == FunctionExprent.FUNCTION_EQ ||
                 fun.getFuncType() == FunctionExprent.FUNCTION_NE) {
          return fun.getLstOperands().get(0);
        }
        else {
          return exp;
        }
      }
      else {
        return exp;
      }
    }
  }

  private static Statement getFirstDirectData(Statement stat) {
    if (stat.getExprents() != null && !stat.getExprents().isEmpty()) {
      return stat;
    }

    for (Statement tmp : stat.getStats()) {
      Statement ret = getFirstDirectData(tmp);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  private static Exprent getUncast(Exprent exp) {
    if (exp.type == Exprent.EXPRENT_FUNCTION) {
      FunctionExprent func = (FunctionExprent)exp;
      if (func.getFuncType() == FunctionExprent.FUNCTION_CAST) {
        return getUncast(func.getLstOperands().get(0));
      }
    }
    return exp;
  }

  private static InvocationExprent asInvocationExprent(Exprent exp) {
    exp = getUncast(exp);
    if (exp.type == Exprent.EXPRENT_INVOCATION) {
      return (InvocationExprent) exp;
    }
    return null;
  }

  private static boolean isIteratorCall(Exprent exp) {
    final InvocationExprent iexp = asInvocationExprent(exp);
    if (iexp == null) {
      return false;
    }
    final org.jetbrains.java.decompiler.struct.gen.MethodDescriptor descriptor = iexp.getDescriptor();
    if (!DecompilerContext.getStructContext().instanceOf(descriptor.ret.value, "java/util/Iterator")) {
      return false;
    }
    final String name = iexp.getName();
    return "iterator".equals(name) ||
           "listIterator".equals(name);
  }

  private static boolean isHasNextCall(Exprent exp) {
    final InvocationExprent iexp = asInvocationExprent(exp);
    if (iexp == null) {
      return false;
    }
    if (!DecompilerContext.getStructContext().instanceOf(iexp.getClassname(), "java/util/Iterator")) {
      return false;
    }
    return "hasNext".equals(iexp.getName()) && "()Z".equals(iexp.getStringDescriptor());
  }

  private static boolean isNextCall(Exprent exp) {
    final InvocationExprent iexp = asInvocationExprent(exp);
    if (iexp == null) {
      return false;
    }
    if (!DecompilerContext.getStructContext().instanceOf(iexp.getClassname(), "java/util/Iterator")) {
      return false;
    }
    return "next".equals(iexp.getName()) && "()Ljava/lang/Object;".equals(iexp.getStringDescriptor());
  }

  private static boolean isNextUnboxing(Exprent exprent) {
    Exprent exp = getUncast(exprent);
    if (exp.type != Exprent.EXPRENT_INVOCATION)
      return false;
    InvocationExprent inv = (InvocationExprent)exp;
    return inv.isUnboxingCall() && isNextCall(inv.getInstance());
  }

  public static boolean makeDoWhileLoops(RootStatement root) {
    if (makeDoWhileRec(root)) {
      SequenceHelper.condenseSequences(root);
      return true;
    }
    
    return false;
  }

  private static boolean makeDoWhileRec(Statement stat) {
    boolean ret = false;

    for (Statement st : stat.getStats()) {
      ret |= makeDoWhileRec(st);
    }

    if (stat.type == Statement.TYPE_DO) {
      DoStatement dostat = (DoStatement)stat;
      if (dostat.getLooptype() == DoStatement.LOOP_DO) {
        matchDoWhile(dostat);
        if (dostat.getLooptype() != DoStatement.LOOP_DO) {
          ret = true;
          ValidationHelper.validateStatement((RootStatement) stat.getTopParent());
        }
      }
    }

    return ret;
  }

  private static boolean addContinueOrBreak(DoStatement stat, StatEdge ifedge) {
    Statement outer = stat.getParent();
    while (outer != null && outer.type != Statement.TYPE_SWITCH && outer.type != Statement.TYPE_DO) {
      outer = outer.getParent();
    }

    if (outer != null && (outer.type == Statement.TYPE_SWITCH || ((DoStatement)outer).getLooptype() != DoStatement.LOOP_DO)) {
      Statement parent = stat.getParent();
      if (parent.type != Statement.TYPE_SEQUENCE || parent.getStats().getLast().equals(stat)) {
        // need to insert a break or continue after the loop
        if (ifedge.getDestination().equals(outer)) {
          stat.addSuccessor(new StatEdge(StatEdge.TYPE_CONTINUE, stat, ifedge.getDestination(), outer));
          return true;
        } else if (MergeHelper.isDirectPath(outer, ifedge.getDestination())) {
          stat.addSuccessor(new StatEdge(StatEdge.TYPE_BREAK, stat, ifedge.getDestination(), outer));
          return true;
        }
      }
    }

    return false;
  }

  // Condense an infinite loop with a return at the end and if statement at the beginning.
  //
  // while(true) {
  //   if (...) {
  //     ...
  //   }
  //   return;
  // }
  //
  // into
  //
  // while(...) {
  //   ...
  // }
  // return;
  //
  public static boolean condenseInfiniteLoopsWithReturn(Statement root) {
    boolean ret = condenseInfiniteLoopsWithReturnRec(root);

    if (ret) {
      SequenceHelper.condenseSequences(root);
    }

    return ret;
  }

  private static boolean condenseInfiniteLoopsWithReturnRec(Statement stat) {
    boolean res = false;

    if (stat.type == Statement.TYPE_DO) {
      DoStatement loop = (DoStatement)stat;

      if (loop.getLooptype() == DoStatement.LOOP_DO) {
        res = condenseLoop(loop);
      }
    }

    for (Statement st : new ArrayList<>(stat.getStats())) {
      res |= condenseInfiniteLoopsWithReturnRec(st);
    }

    return res;
  }

  private static boolean condenseLoop(DoStatement stat) {
    if (stat.getFirst().type == Statement.TYPE_SEQUENCE && stat.getSuccessorEdges(StatEdge.TYPE_REGULAR).isEmpty()) {
      Statement first = stat.getFirst();
      int extractStart = extractableFromLoop((SequenceStatement) first, stat);

      if (first.getStats().size() >= 1 && extractStart > 0) {
        Statement firstBody = first.getStats().get(0);
        Statement lastBody = first.getStats().getLast();
        Statement preExtract = first.getStats().get(extractStart - 1);
        List<Statement> extract = new ArrayList<>(first.getStats().subList(extractStart, first.getStats().size()));

        if (firstBody.type == Statement.TYPE_IF && ((IfStatement)firstBody).iftype == IfStatement.IFTYPE_IF && firstBody.getBasichead().getExprents().isEmpty()) {
          List<StatEdge> breaks = lastBody.getSuccessorEdges(StatEdge.TYPE_BREAK);

          if (!breaks.isEmpty()) {
            if (breaks.get(0).getDestination().type == Statement.TYPE_DUMMYEXIT) {
              Set<StatEdge> edges = new HashSet<>();
              TryWithResourcesProcessor.findEdgesLeaving(firstBody, firstBody, edges);

              // Make sure first statement has continue!
              boolean continueFound = false;
              for (StatEdge edge : edges) {
                if (edge.getDestination() == stat && edge.getType() == StatEdge.TYPE_CONTINUE) {
                  continueFound = true;
                  break;
                }
              }

              // No continue, won't be valid when extraction happens
              if (!continueFound) {
                return false;
              }

              // Process predecessor edges
              for (Statement st : extract) {
                for (StatEdge edge : st.getAllPredecessorEdges()) {
                  if (edge.getType() == StatEdge.TYPE_REGULAR && edge.getSource() == preExtract) {
                    preExtract.removeSuccessor(edge);
                  }

                  if (edge.getType() == StatEdge.TYPE_BREAK) {
                    if (stat.containsStatementStrict(edge.getSource())) {
                      stat.addLabeledEdge(edge);
                    }
                  }
                }
              }

              // Move block outside loop
              extract.add(0, stat);
              SequenceStatement seq = new SequenceStatement(extract);

              // Remove last statement from stat
              for (Statement st : new ArrayList<>(extract)) {
                stat.getFirst().getStats().removeWithKey(st.id);
              }

              // Replace loop with sequence
              stat.replaceWith(seq);

              for (StatEdge edge : new ArrayList<>(seq.getLabelEdges())) {
                stat.addLabeledEdge(edge);
              }

              // Fix continues being held by the sequence instead of the loop
              for (StatEdge edge : seq.getPredecessorEdges(StatEdge.TYPE_CONTINUE)) {
                if (stat.containsStatementStrict(edge.getSource())) {
                  seq.removePredecessor(edge);
                  edge.getSource().changeEdgeNode(Statement.DIRECTION_FORWARD, edge, stat);
                  stat.addPredecessor(edge);
                }
              }

              seq.setAllParent();

              // Edge from loop to next
              Statement next = extract.get(1);
              stat.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, stat, next));

              // Replace first statement from sequence to if statement body
              Statement ifstat = ((IfStatement) firstBody).getIfstat();
              first.replaceWith(ifstat);
              // Remove if edge
              ((IfStatement) firstBody).getIfEdge().getDestination().removeSuccessor(((IfStatement) firstBody).getIfEdge());

              stat.setFirst(ifstat);

              stat.setAllParent();

              // while(true) -> while (...)
              stat.setLooptype(DoStatement.LOOP_WHILE);

              // No negation needed
              stat.setConditionExprent(((IfStatement)firstBody).getHeadexprent().getCondition());

              stat.getFirst().setAllParent();

              return true;
            }
          }
        }
      }
    }

    return false;
  }

  private static int extractableFromLoop(SequenceStatement seq, DoStatement loop) {
    List<Statement> noContinue = new ArrayList<>();
    for (int i = 0; i < seq.getStats().size(); i++) {
      Statement stat = seq.getStats().get(i);

      Set<StatEdge> edges = new HashSet<>();
      TryWithResourcesProcessor.findEdgesLeaving(stat, stat, edges);

      boolean continueFound = false;
      for (StatEdge edge : edges) {
        if (edge.getDestination() == loop && edge.getType() == StatEdge.TYPE_CONTINUE) {
          continueFound = true;
          break;
        }
      }

      if (continueFound) {
        continue;
      }

      if (i > 0) {
        Statement last = seq.getStats().get(i - 1);

        if (last.getAllSuccessorEdges().size() == 1) {
          StatEdge edge = last.getAllSuccessorEdges().get(0);

          if (edge.getType() == StatEdge.TYPE_REGULAR && edge.getDestination() == stat) {
            noContinue.add(stat);
          }
        }
      }
    }

    if (noContinue.isEmpty()) {
      return -1;
    }

    return seq.getStats().indexOf(noContinue.get(0));
  }
}
