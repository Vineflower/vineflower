package org.jetbrains.java.decompiler.modules.decompiler.decompose;

import org.jetbrains.java.decompiler.modules.decompiler.decompose.DomBlocks.*;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement.EdgeDirection;
import org.jetbrains.java.decompiler.util.collections.ListStack;

import java.util.*;
import java.util.stream.Collectors;

// Original comment before it was removed here: https://github.com/JetBrains/intellij-community/commit/44a59462e45ac69bb7c9daa75c3db33446afedf0
//  --------------------------------------------------------------------
//    Algorithm
//  --------------------------------------------------------------------
//  DFS(G)
//  {
//  make a new vertex x with edges x->v for all v
//  initialize a counter N to zero
//  initialize list L to empty
//  build directed tree T, initially a single vertex {x}
//  visit(x)
//  }
//
//  visit(p)
//  {
//  add p to L
//  dfsnum(p) = N
//  increment N
//  low(p) = dfsnum(p)
//  for each edge p->q
//      if q is not already in T
//      {
//      add p->q to T
//      visit(q)
//      low(p) = min(low(p), low(q))
//      } else low(p) = min(low(p), dfsnum(q))
//
//  if low(p)=dfsnum(p)
//  {
//      output "component:"
//      repeat
//      remove last element v from L
//      output v
//      remove v from G
//      until v=p
//  }
//  }
//  --------------------------------------------------------------------

// Original algorithm: https://www.ics.uci.edu/~eppstein/161/960220.html
// Improved based on the description here: https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
// Improvements include using the onStack map to skip visits that aren't needed
public final class StrongConnectivityHelper2 {
  public record Results(
    List<List<DomBlock>> components,
    Map<DomBlock, Integer> statementToComponentMap,
    List<Set<Integer>> successorMap,
    List<Set<Integer>> predecessorMap,
    List<Set<DomBlock>> entryPoints,
    List<Set<DomBlock>> exitPoints,
    Set<Integer> exits
  ) {
  }

  private final List<DomBlock> sources;
  private final Set<DomBlock> sourceSet;
  private final List<List<DomBlock>> components = new ArrayList<>(); // List of strongly connected components, each entry is a list of statements that compose the component
  private final Set<DomBlock> processed = new HashSet<>(); // Already processed statements, persistent
  private final ListStack<DomBlock> stack = new ListStack<>(); // Stack of statements currently being tracked
  private final Set<DomBlock> visitedStatements = new HashSet<>(); // Already seen statements
  private final Map<DomBlock, Integer> indexMap = new HashMap<>(); // Statement -> index
  private final Map<DomBlock, Integer> lowLinkMap = new HashMap<>(); // Statement -> lowest index of any statement reachable from this one
  private final Map<DomBlock, Boolean> onStack = new HashMap<>(); // Statement -> whether this statement is on the stack or not
  private int index; // Index of each statement

  private StrongConnectivityHelper2(List<DomBlock> doms, DomBlock entryPoint) {
    this.sources = doms;
    this.sourceSet = new HashSet<>(doms);

    visitTree(entryPoint);
//
//    for (DomBlock domBlock : doms) {
//      if (domBlock == entryPoint) {
//        continue;
//      }
//      visitTree(domBlock);
//    }
//    visitTree(stat.getFirst());
//
//    for (Statement st : stat.getStats()) {
//      if (!this.processed.contains(st) && st.getPredecessorEdges(Statement.STATEDGE_DIRECT_ALL).isEmpty()) {
//        visitTree(st);
//      }
//    }
//
//    // should not find any more nodes! FIXME: ??
//    // TODO: add validation that this won't happen
//    for (Statement st : stat.getStats()) {
//      if (!this.processed.contains(st)) {
//        visitTree(st);
//      }
//    }
  }

  private void visitTree(DomBlock stat) {
    this.stack.clear();
    this.index = 0;
    this.visitedStatements.clear();
    this.indexMap.clear();
    this.lowLinkMap.clear();
    this.onStack.clear();

    // Visit statement, calculate strong connectivity
    this.visitedStatements.add(stat);
    visit(stat);

    // Add all visited statements to the processed set
    this.processed.addAll(this.visitedStatements);
    this.processed.add(stat);
  }

  private List<DomBlock> getNeighbours(DomBlock stat) {
    return stat.getAllSuccessors().stream()
      .filter(edge -> edge.getType().isUnknown())
      .map(DomEdge::getDestination)
      .filter(this.sourceSet::contains)
      .collect(Collectors.toList());
  }

  private void visit(DomBlock stat) {
    this.stack.push(stat);
    this.indexMap.put(stat, this.index);
    this.lowLinkMap.put(stat, this.index);
    this.index++;
    this.onStack.put(stat, true);

    // Get all neighbor successors
    List<DomBlock> successors = this.getNeighbours(stat);
    // Remove the ones we've already processed
    successors.removeAll(this.processed);

    for (DomBlock succ : successors) {
      int newValue;

      if (this.visitedStatements.contains(succ)) { // Defined index, already visited
        if (!this.onStack.get(succ)) { // If this statement isn't on the stack, skip processing
          continue;
        }

        // New value is the index of the current statement, since we haven't seen this yet
        newValue = this.indexMap.get(succ);
      } else { // Undefined index, haven't visited yet
        //
        this.visitedStatements.add(succ);
        visit(succ); // Recurse

        // Get the low link value and set as the new value
        newValue = this.lowLinkMap.get(succ);
      }

      // Update low link values with the new value
      this.lowLinkMap.put(stat, Math.min(this.lowLinkMap.get(stat), newValue));
    }

    // If the lowlink of the current statement and the index is the same, it means that we're at the root
    if (this.lowLinkMap.get(stat).intValue() == this.indexMap.get(stat).intValue()) {
      // Start new strongly connected component
      List<DomBlock> component = new ArrayList<>();

      DomBlock v;

      do {
        // Pop off statement from stack
        v = this.stack.pop();
        // No longer on stack
        this.onStack.put(v, false);

        // Add to component
        component.add(v);
      }
      while (v != stat); // Repeat for as long as the tested component isn't the root

      // Add component to list
      this.components.add(component);
    }
  }

  private Results getResults() {
    Map<DomBlock, Integer> statementToComponentMap = new HashMap<>();
    List<Set<Integer>> successorMap = new ArrayList<>(this.components.size());
    List<Set<Integer>> predecessorMap = new ArrayList<>(this.components.size());
    List<Set<DomBlock>> entryPoints = new ArrayList<>(this.components.size());
    List<Set<DomBlock>> exitPoints = new ArrayList<>(this.components.size());
    Set<Integer> exits = new HashSet<>();

    for (int i = 0; i < this.components.size(); i++) {
      List<DomBlock> component = this.components.get(i);
      for (var stat : component) {
        statementToComponentMap.put(stat, i);
      }
      successorMap.add(new HashSet<>());
      predecessorMap.add(new HashSet<>());
      entryPoints.add(new HashSet<>());
      exitPoints.add(new HashSet<>());
    }

    for (int i = 0; i < this.components.size(); i++) {
      List<DomBlock> component = this.components.get(i);
      for (var stat : component) {
        for (var succ : this.getNeighbours(stat)) {
          int succComponent = statementToComponentMap.get(succ);
          if (succComponent == i) {
            continue;  // Skip internal edges
          }
          successorMap.get(i).add(succComponent);
          predecessorMap.get(succComponent).add(i);
          entryPoints.get(succComponent).add(succ);
          exitPoints.get(i).add(stat);
        }
      }
    }

    // Add the first statement as an entry point
    DomBlock first = this.sources.get(0);
    entryPoints.get(statementToComponentMap.get(first)).add(first);

    for (int i = 0; i < exitPoints.size(); i++) {
      if (exitPoints.get(i).isEmpty()) {
        exits.add(i);
      }
    }

    return new Results(
      this.components,
      statementToComponentMap,
      successorMap,
      predecessorMap,
      entryPoints,
      exitPoints,
      exits
    );
  }

  public static Results analyse(List<DomBlock> doms, DomBlock entryPoint) {
    return new StrongConnectivityHelper2(doms, entryPoint).getResults();
  }
}