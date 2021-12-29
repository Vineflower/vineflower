// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;

import java.util.*;
import java.util.Map.Entry;


public final class LabelHelper {


  public static void cleanUpEdges(RootStatement root) {

    resetAllEdges(root);

    removeNonImmediateEdges(root);

    liftClosures(root);

    lowContinueLabels(root, new LinkedHashSet<>());

    lowClosures(root);
  }

  public static void identifyLabels(RootStatement root) {

    setExplicitEdges(root);

    hideDefaultSwitchEdges(root);

    processStatementLabel(root);

    setRetEdgesUnlabeled(root);
  }

  private static void liftClosures(Statement stat) {

    for (StatEdge edge : stat.getAllSuccessorEdges()) {
      switch (edge.getType()) {
        case StatEdge.TYPE_CONTINUE:
          // If the continue is pointing somewhere that is not it's enclosed statement, move it to it's enclosed statement (and remove it from it's previous closure)
          if (edge.getDestination() != edge.closure) {
            edge.getDestination().addLabeledEdge(edge);
          }
          break;
        case StatEdge.TYPE_BREAK:
          Statement dest = edge.getDestination();

          // Exclude return edges
          if (dest.type != Statement.TYPE_DUMMYEXIT) {
            Statement parent = dest.getParent();

            List<Statement> lst = new ArrayList<>();
            if (parent.type == Statement.TYPE_SEQUENCE) {
              lst.addAll(parent.getStats());
            } else if (parent.type == Statement.TYPE_SWITCH) {
              lst.addAll(((SwitchStatement)parent).getCaseStatements());
            }

            // TODO: Used to be 0, is 1 valid? Doesn't make sense for a break to target the front of a sequence!
            for (int i = 1; i < lst.size(); i++) {
              if (lst.get(i) == dest) {
                // Add labeled break to the last statement behind the target, lifting it from the statement that it used to be a label of.
                // This is used to lift labels after inlining.
                lst.get(i - 1).addLabeledEdge(edge);
                break;
              }
            }
          }
      }
    }

    for (Statement st : stat.getStats()) {
      liftClosures(st);
    }
  }

  // Removes any breaks and continues from statements that can't have edges to basic blocks
  private static void removeNonImmediateEdges(Statement stat) {

    for (Statement st : stat.getStats()) {
      removeNonImmediateEdges(st);
    }

    if (!stat.hasBasicSuccEdge()) {
      for (StatEdge edge : stat.getSuccessorEdges(StatEdge.TYPE_CONTINUE | StatEdge.TYPE_BREAK)) {
        stat.removeSuccessor(edge);
      }
    }
  }

  public static void lowContinueLabels(Statement stat, HashSet<StatEdge> edges) {

    boolean ok = (stat.type != Statement.TYPE_DO);
    if (!ok) {
      DoStatement dostat = (DoStatement)stat;
      ok = dostat.getLooptype() == DoStatement.LOOP_DO ||
           dostat.getLooptype() == DoStatement.LOOP_WHILE ||
           (dostat.getLooptype() == DoStatement.LOOP_FOR && dostat.getIncExprent() == null);
    }

    if (ok) {
      edges.addAll(stat.getPredecessorEdges(StatEdge.TYPE_CONTINUE));
    }

    if (ok && stat.type == Statement.TYPE_DO) {
      for (StatEdge edge : edges) {
        if (stat.containsStatementStrict(edge.getSource())) {

          edge.getDestination().removePredecessor(edge);
          edge.getSource().changeEdgeNode(Statement.DIRECTION_FORWARD, edge, stat);
          stat.addPredecessor(edge);

          stat.addLabeledEdge(edge);
        }
      }
    }

    for (Statement st : stat.getStats()) {
      if (st == stat.getFirst()) {
        lowContinueLabels(st, edges);
      } else {
        lowContinueLabels(st, new LinkedHashSet<>());
      }
    }
  }

  public static void lowClosures(Statement stat) {

    for (StatEdge edge : new ArrayList<>(stat.getLabelEdges())) {

      if (edge.getType() == StatEdge.TYPE_BREAK) {  // FIXME: ?
        for (Statement st : stat.getStats()) {
          if (st.containsStatementStrict(edge.getSource())) {
            if (MergeHelper.isDirectPath(st, edge.getDestination())) {
              st.addLabeledEdge(edge);
            }
          }
        }
      }
    }

    for (Statement st : stat.getStats()) {
      lowClosures(st);
    }
  }

  // Makes all edges explicit and labeled for further processing
  private static void resetAllEdges(Statement stat) {

    for (Statement st : stat.getStats()) {
      resetAllEdges(st);
    }

    for (StatEdge edge : stat.getAllSuccessorEdges()) {
      edge.explicit = true;
      edge.labeled = true;
    }
  }

  // Removes the labels on edges that are returns as they cannot be labeled
  private static void setRetEdgesUnlabeled(RootStatement root) {
    Statement exit = root.getDummyExit();
    for (StatEdge edge : exit.getAllPredecessorEdges()) {
      List<Exprent> lst = edge.getSource().getExprents();
      if (edge.getType() == StatEdge.TYPE_FINALLYEXIT || (lst != null && !lst.isEmpty() &&
                                                          lst.get(lst.size() - 1).type == Exprent.EXPRENT_EXIT)) {
        edge.labeled = false;
      }
    }
  }

  private static HashMap<Statement, List<StatEdge>> setExplicitEdges(Statement stat) {

    HashMap<Statement, List<StatEdge>> mapEdges = new HashMap<>();

    if (stat.getExprents() != null) {
      return mapEdges;
    }


    switch (stat.type) {
      case Statement.TYPE_TRYCATCH:
      case Statement.TYPE_CATCHALL:

        for (Statement st : stat.getStats()) {
          HashMap<Statement, List<StatEdge>> mapEdges1 = setExplicitEdges(st);
          processEdgesWithNext(st, mapEdges1, null);

          if (stat.type == Statement.TYPE_TRYCATCH || st == stat.getFirst()) { // edges leaving a finally catch block are always explicit
            // merge the maps
            if (mapEdges1 != null) {
              for (Entry<Statement, List<StatEdge>> entr : mapEdges1.entrySet()) {
                if (mapEdges.containsKey(entr.getKey())) {
                  mapEdges.get(entr.getKey()).addAll(entr.getValue());
                }
                else {
                  mapEdges.put(entr.getKey(), entr.getValue());
                }
              }
            }
          }
        }

        break;
      case Statement.TYPE_DO:
        mapEdges = setExplicitEdges(stat.getFirst());
        processEdgesWithNext(stat.getFirst(), mapEdges, stat);
        break;
      case Statement.TYPE_IF:
        IfStatement ifstat = (IfStatement)stat;
        // head statement is a basic block
        if (ifstat.getIfstat() == null) { // empty if
          processEdgesWithNext(ifstat.getFirst(), mapEdges, null);
        }
        else {
          mapEdges = setExplicitEdges(ifstat.getIfstat());
          processEdgesWithNext(ifstat.getIfstat(), mapEdges, null);

          HashMap<Statement, List<StatEdge>> mapEdges1 = null;
          if (ifstat.getElsestat() != null) {
            mapEdges1 = setExplicitEdges(ifstat.getElsestat());
            processEdgesWithNext(ifstat.getElsestat(), mapEdges1, null);
          }

          // merge the maps
          if (mapEdges1 != null) {
            for (Entry<Statement, List<StatEdge>> entr : mapEdges1.entrySet()) {
              if (mapEdges.containsKey(entr.getKey())) {
                mapEdges.get(entr.getKey()).addAll(entr.getValue());
              }
              else {
                mapEdges.put(entr.getKey(), entr.getValue());
              }
            }
          }
        }
        break;
      case Statement.TYPE_ROOT:
        mapEdges = setExplicitEdges(stat.getFirst());
        processEdgesWithNext(stat.getFirst(), mapEdges, ((RootStatement)stat).getDummyExit());
        break;
      case Statement.TYPE_SEQUENCE:
        int index = 0;
        while (index < stat.getStats().size() - 1) {
          Statement st = stat.getStats().get(index);
          processEdgesWithNext(st, setExplicitEdges(st), stat.getStats().get(index + 1));
          index++;
        }

        Statement st = stat.getStats().get(index);
        mapEdges = setExplicitEdges(st);
        processEdgesWithNext(st, mapEdges, null);
        break;
      case Statement.TYPE_SWITCH:
        SwitchStatement swst = (SwitchStatement)stat;

        for (int i = 0; i < swst.getCaseStatements().size() - 1; i++) {
          Statement stt = swst.getCaseStatements().get(i);
          Statement stnext = swst.getCaseStatements().get(i + 1);

          if (stnext.getExprents() != null && stnext.getExprents().isEmpty() && !stnext.getAllSuccessorEdges().isEmpty()) {
            stnext = stnext.getAllSuccessorEdges().get(0).getDestination();
          }
          processEdgesWithNext(stt, setExplicitEdges(stt), stnext);
        }

        int last = swst.getCaseStatements().size() - 1;
        if (last >= 0) { // empty switch possible
          Statement stlast = swst.getCaseStatements().get(last);
          if (stlast.getExprents() != null && stlast.getExprents().isEmpty() && !stlast.getAllSuccessorEdges().isEmpty()) {
            StatEdge edge = stlast.getAllSuccessorEdges().get(0);
            mapEdges.put(edge.getDestination(), new ArrayList<>(Collections.singletonList(edge)));
          }
          else {
            mapEdges = setExplicitEdges(stlast);
            processEdgesWithNext(stlast, mapEdges, null);
          }
        }

        break;
      case Statement.TYPE_SYNCRONIZED:
        SynchronizedStatement synstat = (SynchronizedStatement)stat;

        processEdgesWithNext(synstat.getFirst(), setExplicitEdges(stat.getFirst()), synstat.getBody()); // FIXME: basic block?
        mapEdges = setExplicitEdges(synstat.getBody());
        processEdgesWithNext(synstat.getBody(), mapEdges, null);
    }


    return mapEdges;
  }

  private static void processEdgesWithNext(Statement stat, HashMap<Statement, List<StatEdge>> mapEdges, Statement next) {

    StatEdge statedge = null;

    List<StatEdge> lstSuccs = stat.getAllSuccessorEdges();
    if (!lstSuccs.isEmpty()) {
      statedge = lstSuccs.get(0);

      if (statedge.getDestination() == next) {
        statedge.explicit = false;
        statedge = null;
      }
      else {
        next = statedge.getDestination();
      }
    }

    // no next for a do statement
    if (stat.type == Statement.TYPE_DO && ((DoStatement)stat).getLooptype() == DoStatement.LOOP_DO) {
      next = null;
    }

    // FIXME: Horrible and bad!! This is in the wrong place and shouldn't be using label edges!!
    // Make sure that yield edges are not explicit or labeled, to prevent exit condensation
    if (stat.type == Statement.TYPE_SWITCH && ((SwitchStatement)stat).isPhantom()) {
      for (StatEdge edge : stat.getLabelEdges()) {
        edge.explicit = false;
        edge.labeled = false;
      }
    }

    if (next == null) {
      if (mapEdges.size() == 1) {
        List<StatEdge> lstEdges = mapEdges.values().iterator().next();
        if (lstEdges.size() > 1 && mapEdges.keySet().iterator().next().type != Statement.TYPE_DUMMYEXIT) {
          StatEdge edge_example = lstEdges.get(0);

          Statement closure = stat.getParent();
          if (!closure.containsStatementStrict(edge_example.closure)) {
            closure = edge_example.closure;
          }

          StatEdge newedge = new StatEdge(edge_example.getType(), stat, edge_example.getDestination(), closure);
          stat.addSuccessor(newedge);

          for (StatEdge edge : lstEdges) {
            edge.explicit = false;
          }

          mapEdges.put(newedge.getDestination(), new ArrayList<>(Collections.singletonList(newedge)));
        }
      }
    }
    else {

      boolean implfound = false;

      for (Entry<Statement, List<StatEdge>> entr : mapEdges.entrySet()) {
        if (entr.getKey() == next) {
          for (StatEdge edge : entr.getValue()) {
            if (stat.type == Statement.TYPE_DO) {

              // Edges that contain it's own loop as a closure are always explicit, as removing them can alter control flow [TestLoopBreak]
              if (edge.closure == stat) {
                continue;
              }
            }

            if (edge.closure != null && edge.closure.type == Statement.TYPE_DO && stat.type == Statement.TYPE_IF && edge.getDestination().type == Statement.TYPE_DUMMYEXIT) {
              continue;
            }

            edge.explicit = false;
          }
          implfound = true;
          break;
        }
      }

      if (stat.getAllSuccessorEdges().isEmpty() && !implfound) {
        List<StatEdge> lstEdges = null;
        for (Entry<Statement, List<StatEdge>> entr : mapEdges.entrySet()) {
          if (entr.getKey().type != Statement.TYPE_DUMMYEXIT &&
              (lstEdges == null || entr.getValue().size() > lstEdges.size())) {
            lstEdges = entr.getValue();
          }
        }

        if (lstEdges != null && lstEdges.size() > 1) {
          StatEdge edge_example = lstEdges.get(0);

          Statement closure = stat.getParent();
          if (!closure.containsStatementStrict(edge_example.closure)) {
            closure = edge_example.closure;
          }

          StatEdge newedge = new StatEdge(edge_example.getType(), stat, edge_example.getDestination(), closure);
          stat.addSuccessor(newedge);

          for (StatEdge edge : lstEdges) {
            edge.explicit = false;
          }
        }
      }

      mapEdges.clear();
    }

    if (statedge != null) {
      mapEdges.put(statedge.getDestination(), new ArrayList<>(Collections.singletonList(statedge)));
    }
  }

  public static boolean hideDefaultSwitchEdges(Statement stat) {
    boolean res = false;
    if (stat.type == Statement.TYPE_SWITCH) {
      SwitchStatement swst = (SwitchStatement)stat;

      int last = swst.getCaseStatements().size() - 1;
      if (last >= 0) { // empty switch possible
        Statement stlast = swst.getCaseStatements().get(last);

        if (stlast.getExprents() != null && stlast.getExprents().isEmpty()) {
          List<StatEdge> edges = stlast.getAllSuccessorEdges();
          // If we don't have an edge from this statement or if the edge that we have isn't explicit, delete the default edge
          if (edges.isEmpty() || !edges.get(0).explicit) {
            List<StatEdge> lstEdges = swst.getCaseEdges().get(last);
            lstEdges.remove(swst.getDefaultEdge());

            if (lstEdges.isEmpty()) {
              swst.getCaseStatements().remove(last);
              swst.getCaseEdges().remove(last);
            }

            res = true;
          }
        }
      }
    }

    for (Statement st : stat.getStats()) {
      res |= hideDefaultSwitchEdges(st);
    }

    return res;
  }

  private static class LabelSets {
    private final Set<Statement> breaks = new HashSet<>();
    private final Set<Statement> continues = new HashSet<>();
  }

  private static LabelSets processStatementLabel(Statement stat) {
    LabelSets sets = new LabelSets();

    if (stat.getExprents() == null) {
      for (Statement st : stat.getStats()) {
        LabelSets nested = processStatementLabel(st);
        sets.breaks.addAll(nested.breaks);
        sets.continues.addAll(nested.continues);
      }

      boolean shieldType = (stat.type == Statement.TYPE_DO || stat.type == Statement.TYPE_SWITCH);
      if (shieldType) {
        for (StatEdge edge : stat.getLabelEdges()) {
          if (edge.explicit && ((edge.getType() == StatEdge.TYPE_BREAK && sets.breaks.contains(edge.getSource())) ||
                                (edge.getType() == StatEdge.TYPE_CONTINUE && sets.continues.contains(edge.getSource())))) {
            edge.labeled = false;
          }
        }
      }

      switch (stat.type) {
        case Statement.TYPE_DO:
          sets.continues.clear();
        case Statement.TYPE_SWITCH:
          sets.breaks.clear();
      }
    }

    sets.breaks.add(stat);
    sets.continues.add(stat);

    return sets;
  }

  // Handles switches in loops, so switch breaks don't become continues
  //
  // Also processes labeled continues to make them into breaks if they are the last statement inside the closure statement
  // As so:
  //
  // label:
  // while(...) {
  //   ...
  //   while(...) {
  //     ...
  //     if (...) {
  //       continue label;
  //     }
  //   }
  // }
  //
  // will turn into
  //
  // while(...) {
  //   ...
  //   while(...) {
  //     ...
  //     if (...) {
  //       break;
  //     }
  //   }
  // }
  //
  // This is only applicable when this is the last statement within it's parent nest as otherwise it'll be a jump to the next statement instead of a backjump to the loop
  public static boolean replaceContinueWithBreak(Statement stat) {
    boolean res = false;

    if (stat.type == Statement.TYPE_DO) {
      boolean changed;

      // Edges that we've seen already
      Set<StatEdge> seenEdges = new HashSet<>();
      do {
        changed = false;

        List<StatEdge> continues = stat.getPredecessorEdges(StatEdge.TYPE_CONTINUE);

        for (StatEdge edge : continues) {
          if (seenEdges.contains(edge)) {
            continue;
          }

          if (edge.explicit) {
            Statement minclosure = getMinContinueClosure(edge);

            if (minclosure != edge.closure && !InlineSingleBlockHelper.isBreakEdgeLabeled(edge.getSource(), minclosure)) {
              // Continue -> Break
              edge.getSource().changeEdgeType(Statement.DIRECTION_FORWARD, edge, StatEdge.TYPE_BREAK);
              // No more label
              edge.labeled = false;
              // Add labeled edge to the closure
              minclosure.addLabeledEdge(edge);

              // Don't process this edge again
              seenEdges.add(edge);

              res = true;
              changed = true;
            }

            Statement enclosing = findMaxJump(getEnclosingShieldType(edge.getSource()));
            // See if the loop we're in has an unconditional jump
            if (enclosing.type == Statement.TYPE_DO && !enclosing.getSuccessorEdges(StatEdge.TYPE_BREAK).isEmpty()) {
              // Make sure that the break on the enclosing statement points towards the statement that we want to process
              // Because the statement structure is inconsistent at this point we can't consider the actual break edges
              if (getEnclosingShieldType(enclosing.getParent()) == minclosure) {
                // Continue -> Break
                edge.getSource().changeEdgeType(Statement.DIRECTION_FORWARD, edge, StatEdge.TYPE_BREAK);
                // No more label
                edge.labeled = false;
                // Add labeled edge to the closure
                enclosing.addLabeledEdge(edge);

                // Don't process this edge again
                seenEdges.add(edge);

                res = true;
                changed = true;
              }
            }
          }
        }
      } while (changed);
    }

    for (Statement st : stat.getStats()) {
      res |= replaceContinueWithBreak(st);
    }

    return res;
  }

  // Finds the max loop that can be reached by following unconditional break edges
  private static Statement findMaxJump(Statement enclosing) {
    Statement last = enclosing;
    while (enclosing.type == Statement.TYPE_DO && !enclosing.getSuccessorEdges(StatEdge.TYPE_BREAK).isEmpty()) {
      last = enclosing;
      enclosing = getEnclosingShieldType(enclosing.getParent());
    }

    return last;
  }

  // Shield type refers to either loop or switch, mirroring the variable name above
  // Finds the topmost statement that this can continue to, or returns the root statement if none was found (should never happen)
  private static Statement getEnclosingShieldType(Statement stat) {
    Statement res = stat;

    while (res.type != Statement.TYPE_SWITCH && res.type != Statement.TYPE_DO) {
      Statement parent = res.getParent();

      // Return the root statement if we can't find anything
      if (parent == null) {
        break;
      }

      res = parent;
    }

    return res;
  }

  private static Statement getMinContinueClosure(StatEdge edge) {

    Statement closure = edge.closure;
    while (true) {

      boolean found = false;

      for (Statement st : closure.getStats()) {
        if (st.containsStatementStrict(edge.getSource())) {
          if (MergeHelper.isDirectPath(st, edge.getDestination())) {
            closure = st;
            found = true;
            break;
          }
        }
      }

      if (!found) {
        break;
      }
    }

    return closure;
  }
}
