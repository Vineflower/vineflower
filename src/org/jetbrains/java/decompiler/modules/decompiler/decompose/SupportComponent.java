package org.jetbrains.java.decompiler.modules.decompiler.decompose;

import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.util.FastFixedSetFactory.FastFixedSet;

import java.util.*;

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
    Set<Statement> supportedAll = new HashSet<>();
    for (Statement st : component) {
      FastFixedSet<Integer> supReach = mapSupportPoints.get(st.id);

      if (supReach != null) {
        for (StatEdge edge : st.getSuccessorEdgeView(StatEdge.TYPE_REGULAR)) {
          Statement dest = edge.getDestination();

          if (!component.contains(dest)) {
            // Support point supports statement outside of component
            return null;
          } else {
            if (dom.isDominator(st.id, dest.id)) {
              supportedAll.add(dest);
            }
          }
        }

        // no filter needed as this is coming from the component itself
        selfSupportPoints.put(st.id, supReach);
      }
    }

    if (supportedAll.size() != 1) {
      return null;
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

  @Override
  public String toString() {
    return "SupportComponent[" + stats + ", selfSupportPoints=" + selfSupportPoints + ", header=" + supportedPoint + ']';
  }
}
