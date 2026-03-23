// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement.EdgeDirection;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class ExitHelper {
  public static boolean condenseExits(RootStatement root) {
    int changed = integrateExits(root);

    if (changed > 0) {
      cleanUpUnreachableBlocks(root);
      SequenceHelper.condenseSequences(root);
    }

    return (changed > 0);
  }

  private static void cleanUpUnreachableBlocks(Statement stat) {
    boolean found;
    do {
      found = false;

      for (int i = 0; i < stat.getStats().size(); i++) {
        Statement st = stat.getStats().get(i);

        cleanUpUnreachableBlocks(st);

        if (st instanceof SequenceStatement && st.getStats().size() > 1) {

          Statement last = st.getStats().getLast();
          Statement secondlast = st.getStats().get(st.getStats().size() - 2);

          if (!secondlast.hasBasicSuccEdge()) {
            Set<Statement> set = last.getNeighboursSet(Statement.STATEDGE_DIRECT_ALL, EdgeDirection.BACKWARD);
            set.remove(secondlast);

            if (set.isEmpty()) {
              last.getExprents().clear();
              st.getStats().removeWithKey(last.id);
              for (StatEdge succEdge : last.getAllSuccessorEdges()) {
                succEdge.remove();
              }
              for (StatEdge predEdge : last.getAllPredecessorEdges()) {
                predEdge.remove();
              }
              found = true;
              break;
            }
          }
        }
      }
    }
    while (found);
  }

  // Turns break edges into returns where possible.
  //
  // Example:
  //
  // label1: {
  //   if (...) {
  //     break label1;
  //   }
  //   ...
  // }
  // return;
  //
  // will turn into
  //
  // if (...) {
  //   return;
  // }
  // ...
  //
  // TODO: This function needs to be refactored into multiple functions in the future.
  private static int integrateExits(Statement stat) {
    int ret = 0;

    if (stat.getExprents() == null) {
      while (true) {
        int changed = 0;

        for (Statement st : stat.getStats()) {
          changed = integrateExits(st);
          if (changed > 0) {
            ret = 1;
            break;
          }
        }

        if (changed == 0) {
          break;
        }
      }

      if (stat instanceof IfStatement ifst && ifst.getIfstat() == null) {
        StatEdge ifedge = ifst.getIfEdge();
        Statement dest = isExitEdge(ifedge);

        StatEdge ifBreakEdge = null;
        if (dest == null) {
          // If it's not the if edge, also check the first break out of the if.
          // The real edge may also be a break from the if's first successor (and the basichead breaks to default stat)
          if (ifst.hasSuccessor(StatEdge.TYPE_BREAK)) {
            ifBreakEdge = ifst.getSuccessorEdges(StatEdge.TYPE_BREAK).get(0);
            dest = findIfBreakEdge(ifst, ifBreakEdge);
          } else if (!ifst.getSuccessorEdges(StatEdge.TYPE_REGULAR).isEmpty()) {
            Statement adjacentStat = ifst.getSuccessorEdges(StatEdge.TYPE_REGULAR).get(0).getDestination();

            if (adjacentStat.hasSuccessor(StatEdge.TYPE_BREAK)) {
              ifBreakEdge = adjacentStat.getSuccessorEdges(StatEdge.TYPE_BREAK).get(0);
              dest = findIfBreakEdge(ifst, ifBreakEdge);
            }
          }
        }

        if (dest != null) {
          BasicBlockStatement newBlock = BasicBlockStatement.create();
          newBlock.setExprents(DecHelper.copyExprentList(dest.getExprents()));

          // Store the old exit edge before changing any of the below.
          // This is so we can replace the dest with an empty block if needed, and still keep the edge reference.
          StatEdge oldexitedge = dest.getFirstSuccessor();

          if (ifBreakEdge != null) {
            boolean ifstInSeqence = ifst.getParent() instanceof SequenceStatement;
            if (ifstInSeqence) {
              // The if statement being in a sequence means we want more than just the dest's expr list.
              newBlock.getExprents().addAll(0, DecHelper.copyExprentList(ifBreakEdge.getSource().getExprents()));
            }

            SwitchStatement switchStat = null;
            if (ifst.getParent() instanceof SwitchStatement s) {
              switchStat = s;
            } else if (ifstInSeqence && ifst.getParent().getParent() instanceof SwitchStatement s) {
              switchStat = s;
            }

            // If the if is negated e.g. `!var2.equals("1")`, invert it.
            if (switchStat != null
              && ifst.getHeadexprent().getCondition() instanceof FunctionExprent funcExpr
              && funcExpr.getFuncType().equals(FunctionExprent.FunctionType.BOOL_NOT)) {
              // Invert the condition
              ifst.getHeadexprent().setCondition(funcExpr.getLstOperands().get(0));

              // Redirect if -> dest edge to if -> default case and remove dest block
              if (switchStat.getDefaultEdge() != null) {
                final var defaultCase = switchStat.getDefaultEdge().getDestination().getFirstSuccessor().getDestination();
                if (ifedge.getDestination().equals(defaultCase)) {
                  if (ifstInSeqence) {
                    ifst.getSuccessorEdges(StatEdge.TYPE_REGULAR).get(0).changeSource(ifst.getParent());

                    ifBreakEdge.getSource().replaceWithEmpty();
                    ifBreakEdge.getDestination().replaceWithEmpty();
                  } else {
                    ifst.removeSuccessor(ifBreakEdge);
                  }

                  ifst.addSuccessor(new StatEdge(StatEdge.TYPE_BREAK, ifst, defaultCase, ifedge.closure));
                }
              }
            }
          }

          // Link the new block to the if statement
          StatEdge newedge = new StatEdge(StatEdge.TYPE_REGULAR, ifst.getFirst(), newBlock);
          ifst.getFirst().removeSuccessor(ifedge);
          ifst.getFirst().addSuccessor(newedge);
          ifst.setIfEdge(newedge);
          ifst.setIfstat(newBlock);
          ifst.getStats().addWithKey(newBlock, newBlock.id);
          newBlock.setParent(ifst);

          // Re-create the old block's exit edge but for the new block.
          // The edge is exiting to the default case dest / out of the label block that encapsulates the switch.
          StatEdge newexitedge = new StatEdge(StatEdge.TYPE_BREAK, newBlock, oldexitedge.getDestination());
          newBlock.addSuccessor(newexitedge);
          oldexitedge.closure.addLabeledEdge(newexitedge);

          ret = 1;
        }
      }
    }

    if (stat.getAllSuccessorEdges().size() == 1
      && stat.getAllSuccessorEdges().get(0).getType() == StatEdge.TYPE_BREAK
      && stat.getLabelEdges().isEmpty()) {
      Statement parent = stat.getParent();
      if (stat == parent.getFirst() && (parent instanceof IfStatement || parent instanceof SwitchStatement)) {
        return ret;
      }

      StatEdge destedge = stat.getAllSuccessorEdges().get(0);
      Statement dest = isExitEdge(destedge);
      if (dest == null) {
        return ret;
      }

      stat.removeSuccessor(destedge);

      BasicBlockStatement bstat = BasicBlockStatement.create();
      bstat.setExprents(DecHelper.copyExprentList(dest.getExprents()));

      StatEdge oldexitedge = dest.getAllSuccessorEdges().get(0);
      StatEdge newexitedge = new StatEdge(StatEdge.TYPE_BREAK, bstat, oldexitedge.getDestination());
      bstat.addSuccessor(newexitedge);
      oldexitedge.closure.addLabeledEdge(newexitedge);

      SequenceStatement block = new SequenceStatement(Arrays.asList(stat, bstat));
      block.setAllParent();

      parent.replaceStatement(stat, block);
      // LabelHelper.lowContinueLabels not applicable because of forward continue edges
      // LabelHelper.lowContinueLabels(block, new HashSet<StatEdge>());
      // do it by hand
      for (StatEdge prededge : block.getPredecessorEdges(StatEdge.TYPE_CONTINUE)) {
        block.removePredecessor(prededge);
        prededge.getSource().changeEdgeNode(EdgeDirection.FORWARD, prededge, stat);
        stat.addPredecessor(prededge);
        stat.addLabeledEdge(prededge);
      }

      stat.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, stat, bstat));

      for (StatEdge edge : dest.getAllPredecessorEdges()) {
        if (edge.explicit
          || !stat.containsStatementStrict(edge.getSource())
          || !MergeHelper.isDirectPath(edge.getSource().getParent(), bstat)) {
          continue;
        }

        dest.removePredecessor(edge);
        edge.getSource().changeEdgeNode(EdgeDirection.FORWARD, edge, bstat);
        bstat.addPredecessor(edge);

        if (!stat.containsStatementStrict(edge.closure)) {
          stat.addLabeledEdge(edge);
        }
      }

      ret = 2;
    }

    return ret;
  }

  private static Statement isExitEdge(StatEdge edge) {
    Statement dest = edge.getDestination();

    if (edge.getType() == StatEdge.TYPE_BREAK
      && dest instanceof BasicBlockStatement
      && edge.explicit
      && (edge.labeled || isOnlyEdge(edge))
      && edge.canInline) {
      // Don't inline in phantom statements. This can break the statement graph, and cause recursive writing.
      Statement parent = edge.closure;
      while (parent != null) {
        if (parent instanceof SwitchStatement sw && sw.isPhantom()) {
          return null;
        }
        parent = parent.getParent();
      }

      List<Exprent> data = dest.getExprents();

      if (data != null && data.size() == 1) {
        if (data.get(0) instanceof ExitExprent) {
          return dest;
        }
      }
    }

    return null;
  }

  /**
   * Checks to see if a valid break edge from the if block to the desired destination block exists.
   * In this configuration, the if's basichead will actually have a break edge out of the label.
   * The "switch" refers to when the if is actually a case statement in a switch statement.
   * This may be the result of the compiler arbitrarily inverting if conditions,
   *   so the desired if exprents are in a different block.
   * <p>
   * <pre> {@code
   *   label19: {
   *     switch (...) {
   *       case ...:
   *         if (...) {
   *           // Observed break edge found from `ifst.getBasichead()`
   *           break label19;
   *         }
   *
   *         // Real/desired block here!
   *         return 1;
   *       case ...:
   *         ...
   *       default:
   *     }
   *   }
   *
   *   // Usually the default case statement block when the switch has an empty default case
   *   return -1;
   * } </pre>
   * <p>
   * @param ifst the if statement block
   * @param edge the break edge from the if block to the desired destination block ('observed' break edge)
   * @return the desired destination block containing a single exit exprent, or null if no such block was found.
   */
  // Use nullable annotation instead of optional because this function may be in a hot path
  private static @Nullable Statement findIfBreakEdge(IfStatement ifst, StatEdge edge) {
    Statement dest = edge.getDestination();

    // A cheap check to avoid processing unwanted things (see TestSwitchLoop#test with this check disabled)
    //   If there is better and more specific checks for this, please add them instead.
    if (ifst.getBasichead().getExprents() == null || !ifst.getBasichead().getExprents().isEmpty()) {
      return null;
    }

    // It should be a break edge that can be inlined and is explicit
    if (edge.getType() != StatEdge.TYPE_BREAK
      || !(dest instanceof BasicBlockStatement)
      || !edge.explicit
      || !edge.canInline) {
      return null;
    }

    // The edge should be either:
    //   - A labeled edge
    //   - An only edge
    //   - A break edge which breaks out of the switch to an exit
    if (!edge.labeled
      && !isOnlyEdge(edge)
      && (!ifst.getParent().equals(edge.closure)
        || !edge.getDestination().equals(ifst.getParent().getFirstSuccessor().getDestination()))) {
      return null;
    }

    // Don't inline in phantom statements. This can break the statement graph, and cause recursive writing.
    Statement parent = edge.closure;
    while (parent != null) {
      if (parent instanceof SwitchStatement sw && sw.isPhantom()) {
        return null;
      }

      parent = parent.getParent();
    }

    List<Exprent> data = dest.getExprents();
    if (data == null || data.size() != 1 || !(data.get(0) instanceof ExitExprent)) {
      return null;
    }

    return dest;
  }

  private static boolean isOnlyEdge(StatEdge edge) {
    Statement stat = edge.getDestination();

    for (StatEdge ed : stat.getAllPredecessorEdges()) {
      if (ed != edge) {
        if (ed.getType() == StatEdge.TYPE_REGULAR) {
          Statement source = ed.getSource();

          if (source instanceof BasicBlockStatement
            || (source instanceof IfStatement sourceIf && sourceIf.iftype == IfStatement.IFTYPE_IF)
            || (source instanceof DoStatement sourceDo && sourceDo.getLooptype() != DoStatement.Type.INFINITE)) {
            return false;
          }
        } else {
          return false;
        }
      }
    }

    return true;
  }

  // Removes return statements from the ends of methods when they aren't returning a value
  public static boolean removeRedundantReturns(RootStatement root) {
    boolean res = false;
    DummyExitStatement dummyExit = root.getDummyExit();

    for (StatEdge edge : dummyExit.getAllPredecessorEdges()) {
      if (!edge.explicit) {
        Statement source = edge.getSource();
        List<Exprent> lstExpr = source.getExprents();
        if (lstExpr != null && !lstExpr.isEmpty()) {
          Exprent expr = lstExpr.get(lstExpr.size() - 1);
          if (expr instanceof ExitExprent ex) {
            if (ex.getExitType() == ExitExprent.Type.RETURN && ex.getValue() == null) {
              // remove redundant return
              dummyExit.addBytecodeOffsets(ex.bytecode);
              lstExpr.remove(lstExpr.size() - 1);
              res = true;
            }
          }
        }
      }
    }

    return res;
  }

  // Fixes chars being returned when ints are required
  public static boolean adjustReturnType(RootStatement root, MethodDescriptor desc) {
    boolean res = false;
    // Get all statements with returns
    for (StatEdge retEdge : root.getDummyExit().getAllPredecessorEdges()) {
      Statement ret = retEdge.getSource();

      // Get all exprent in statement
      List<Exprent> exprents = ret.getExprents();
      if (exprents != null && !exprents.isEmpty()) {
        // Get return exprent
        Exprent expr = exprents.get(exprents.size() - 1);
        if (expr instanceof ExitExprent ex) {

          List<Exprent> exitExprents = ex.getAllExprents(true);

          // If any of the return expression has constants, adjust them to the return type of the method
          for (Exprent exprent : exitExprents) {
            if (exprent instanceof ConstExprent) {
              ((ConstExprent) exprent).adjustConstType(desc.ret);
              res = true;
            }
          }
        }
      }
    }

    return res;
  }
}