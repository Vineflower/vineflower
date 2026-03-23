// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

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
  private static int integrateExits(Statement stat) {
    int ret = 0;
    Statement dest;

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

        dest = isExitEdge(ifedge);

        StatEdge realIfedge = null;
        if (dest == null) {
          // If it's not the if edge, also check the first break out of the if.
          // The real edge may also be a break from the if's first successor (and the basichead breaks to default stat)
          if (ifst.hasSuccessor(StatEdge.TYPE_BREAK)) {
            realIfedge = ifst.getSuccessorEdges(StatEdge.TYPE_BREAK).get(0);
            dest = isRealIfEdge(ifst, realIfedge);
          } else if (!ifst.getSuccessorEdges(StatEdge.TYPE_REGULAR).isEmpty()) {
            Statement adjacentStat = ifst.getSuccessorEdges(StatEdge.TYPE_REGULAR).get(0).getDestination();

            if (adjacentStat.hasSuccessor(StatEdge.TYPE_BREAK)) {
              realIfedge = adjacentStat.getSuccessorEdges(StatEdge.TYPE_BREAK).get(0);
              dest = isRealIfEdge(ifst, realIfedge);
            }
          }
        }

        if (dest != null) {
          BasicBlockStatement bstat = BasicBlockStatement.create();
          bstat.setExprents(DecHelper.copyExprentList(dest.getExprents()));

          // Store the old exit edge before changing any of the below.
          // This is so we can replace the dest with an empty block if needed, and still keep the edge reference.
          StatEdge oldexitedge = dest.getFirstSuccessor();

          if (realIfedge != null) {
            boolean ifstInSeqence = ifst.getParent() instanceof SequenceStatement;
            if (ifstInSeqence) {
              // The if statement being in a sequence means we want more than just the dest's expr list.
              bstat.getExprents().addAll(0, DecHelper.copyExprentList(realIfedge.getSource().getExprents()));
            }

            SwitchStatement switchStat = null;
            if (ifst.getParent() instanceof SwitchStatement s) {
              switchStat = s;
            } else if (ifstInSeqence && ifst.getParent().getParent() instanceof SwitchStatement s) {
              switchStat = s;
            }

            // If the if is negated e.g. `!var2.equals("1")`
            if (switchStat != null
              && ifst.getHeadexprent().getCondition() instanceof FunctionExprent funcExpr
              && funcExpr.getFuncType().equals(FunctionExprent.FunctionType.BOOL_NOT)) {
              // Invert if condition
              ifst.getHeadexprent().setCondition(funcExpr.getLstOperands().get(0));

              // Redirect if -> dest edge to if -> default case
              if (switchStat.getDefaultEdge() != null) {
                final var defaultCase = switchStat.getDefaultEdge().getDestination().getFirstSuccessor().getDestination();
                if (ifedge.getDestination().equals(defaultCase)) {
                  if (ifstInSeqence) {
                    ifst.getSuccessorEdges(StatEdge.TYPE_REGULAR).get(0).changeSource(ifst.getParent());

                    realIfedge.getSource().replaceWithEmpty();
                    realIfedge.getDestination().replaceWithEmpty();
                  } else {
                    ifst.removeSuccessor(realIfedge);
                  }

                  ifst.addSuccessor(new StatEdge(StatEdge.TYPE_BREAK, ifst, defaultCase, ifedge.closure));
                }
              }
            }
          }

          StatEdge newedge = new StatEdge(StatEdge.TYPE_REGULAR, ifst.getFirst(), bstat);
          ifst.getFirst().removeSuccessor(ifedge);
          ifst.getFirst().addSuccessor(newedge);
          ifst.setIfEdge(newedge);
          ifst.setIfstat(bstat);
          ifst.getStats().addWithKey(bstat, bstat.id);
          bstat.setParent(ifst);

          StatEdge newexitedge = new StatEdge(StatEdge.TYPE_BREAK, bstat, oldexitedge.getDestination());
          bstat.addSuccessor(newexitedge);
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
      dest = isExitEdge(destedge);
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

  private static Statement isRealIfEdge(IfStatement ifst, StatEdge edge) {
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
    if (data != null && data.size() == 1 && data.get(0) instanceof ExitExprent) {
      return dest;
    }

    return null;
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