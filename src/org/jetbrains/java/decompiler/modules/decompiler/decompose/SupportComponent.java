package org.jetbrains.java.decompiler.modules.decompiler.decompose;

import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.util.FastFixedSetFactory.FastFixedSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SupportComponent {
  public final List<Statement> stats;
  // Backedges to loop header
  public final Map<Integer, FastFixedSet<Integer>> selfSupportPoints;
  // Loop header
  public final Statement supportedPoint;

  public SupportComponent(List<Statement> stats, Map<Integer, FastFixedSet<Integer>> selfSupportPoints, Statement supportedPoint) {
    this.stats = stats;
    this.selfSupportPoints = selfSupportPoints;
    this.supportedPoint = supportedPoint;
  }


  public static SupportComponent identify(List<Statement> component, Map<Integer, FastFixedSet<Integer>> mapSupportPoints, DominatorEngine dom) {

    Map<Integer, FastFixedSet<Integer>> selfSupportPoints = new HashMap<>();
    for (Statement st : component) {
      FastFixedSet<Integer> supReach = mapSupportPoints.get(st.id);

      if (supReach != null) {
        for (StatEdge edge : st.getSuccessorEdgeView(StatEdge.TYPE_REGULAR)) {
          if (!component.contains(edge.getDestination())) {
            // Support point supports statement outside of component
            return null;
          }
        }

        // no filter needed as this is coming from the component itself
        selfSupportPoints.put(st.id, supReach);
      }
    }

    if (selfSupportPoints.isEmpty()) {
      return null;
    }

    List<Statement> outgoing = new ArrayList<>();
    for (Statement st : component) {
      // TODO: pred edge view
      for (StatEdge edge : st.getPredecessorEdges(StatEdge.TYPE_REGULAR)) {
        if (!component.contains(edge.getSource())) {
          outgoing.add(st);
        }
      }
    }

    if (outgoing.size() != 1) {
      return null;
    }

    Statement head = outgoing.get(0);
    for (Statement st : component) {
      if (!dom.isDominator(st.id, head.id)) {
        return null;
      }
    }

    return new SupportComponent(component, selfSupportPoints, head);
  }
}
