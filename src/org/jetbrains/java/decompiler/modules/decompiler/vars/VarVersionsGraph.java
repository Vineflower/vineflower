// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.GenericDominatorEngine;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.IGraph;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.IGraphNode;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute.LocalVariable;
import org.jetbrains.java.decompiler.util.collections.ListStack;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;

import java.util.*;

public class VarVersionsGraph {
  public final VBStyleCollection<VarVersionNode, VarVersionPair> nodes = new VBStyleCollection<>();

  private GenericDominatorEngine engine;

  public VarVersionNode createNode(VarVersionPair ver) {
    return this.createNode(ver, null);
  }

  public VarVersionNode createNode(VarVersionPair ver, LocalVariable lvt) {
    VarVersionNode node = new VarVersionNode(ver.var, ver.version, lvt);
    this.nodes.addWithKey(node, ver);
    return node;
  }

  public boolean isDominatorSet(VarVersionNode node, Set<VarVersionNode> domnodes) {
    if (domnodes.size() == 1) {
      return this.engine.isDominator(node, domnodes.iterator().next());
    } else {
      if (domnodes.contains(node)) {
        return true;
      }

      Set<VarVersionNode> seen = new HashSet<>();

      Deque<VarVersionNode> lstNodes = new ArrayDeque<>();
      lstNodes.add(node);

      while (!lstNodes.isEmpty()) {
        VarVersionNode nd = lstNodes.pollFirst();

        if (!seen.add(nd)) {
          continue;
        }

        if (nd.predecessors.isEmpty()) {
          return false;
        }

        for (VarVersionNode pred : nd.predecessors) {
          if (!seen.contains(pred) && !domnodes.contains(pred)) {
            lstNodes.addLast(pred);
          }
        }
      }
    }

    return true;
  }

  public void initDominators() {
    Set<VarVersionNode> roots = new HashSet<>();

    for (VarVersionNode node : this.nodes) {
      if (node.predecessors.isEmpty()) {
        roots.add(node);
      }
    }

    // TODO: optimization!! This is called multiple times for each method and the allocations will add up!
    if (ValidationHelper.VALIDATE) {
      Set<VarVersionNode> reached = rootReachability(roots);
      ValidationHelper.validateTrue(this.nodes.size() == reached.size(), "Cyclic roots detected");
      // If the nodes we reach don't include every node we have, then we need to process further to decompose the cycles
      //noinspection ConstantValue
      if (this.nodes.size() != reached.size()) {
        // Not all nodes are reachable, due to cyclic nodes

        // Find only the nodes that aren't accounted for
        Set<VarVersionNode> intersection = new HashSet<>(this.nodes);
        intersection.removeAll(reached);

        // Var -> [versions]
        Map<Integer, List<Integer>> varMap = new HashMap<>();

        Set<VarVersionNode> visited = new HashSet<>();
        for (VarVersionNode node : intersection) {
          if (visited.contains(node)) {
            continue;
          }

          // DFS to find all nodes reachable from this node
          Set<VarVersionNode> found = this.findReachableNodes(node);
          // Skip all the found nodes from this node in the future
          visited.addAll(found);

          // For every node that we found, keep track of the var index and the versions of each node
          // Each disjoint set *should* only reference a single var, so we operate under that assumption and keep track of the versions based on the var index
          // If this isn't true, then this algorithm won't find every cyclic root as it will account multiple disjoint sets as one!
          for (VarVersionNode foundNode : found) {
            varMap.computeIfAbsent(foundNode.var, k -> new ArrayList<>()).add(foundNode.version);
          }
        }

        for (Integer var : varMap.keySet()) {
          // Sort versions
          varMap.get(var).sort(Comparator.naturalOrder());

          // First version is the lowest version, so that can be considered as the root
          VarVersionPair pair = new VarVersionPair(var, varMap.get(var).get(0));

          // Add to existing roots
          roots.add(this.nodes.getWithKey(pair));
        }

        // TODO: needs another validation pass?
      }
    }

    this.engine = new GenericDominatorEngine(new IGraph() {
      @Override
      public List<? extends IGraphNode> getReversePostOrderList() {
        return getReversedPostOrder(roots);
      }

      @Override
      public Set<? extends IGraphNode> getRoots() {
        return roots;
      }
    });

    this.engine.initialize();
  }

  /**
   * Returns the set of nodes that are reachable by the given node.
   * These are all the nodes that could read a value set at the start node
   */
  private Set<VarVersionNode> findReachableNodes(VarVersionNode start) {
    Set<VarVersionNode> visited = new HashSet<>();
    ListStack<VarVersionNode> stack = new ListStack<>();
    stack.add(start);

    while (!stack.isEmpty()) {
      VarVersionNode node = stack.pop();

      if (visited.add(node)) {
        stack.addAll(node.successors);
      }
    }

    return visited;
  }

  /**
   * Returns the set of nodes that are reachable by the given roots.
   */
  public static Set<VarVersionNode> rootReachability(Set<VarVersionNode> roots) {
    Set<VarVersionNode> visited = new HashSet<>();

    ListStack<VarVersionNode> stack = new ListStack<>(roots);

    while (!stack.isEmpty()) {
      VarVersionNode node = stack.pop();

      if (visited.add(node)) {
        stack.addAll(node.successors);
      }
    }

    return visited;
  }

  public boolean areVarsAnalogous(int varBase, int varCheck) {
    Deque<VarVersionNode> stack = new ArrayDeque<>();
    Set<VarVersionNode> visited = new HashSet<>();

    VarVersionNode start = this.nodes.getWithKey(new VarVersionPair(varBase, 1));
    stack.add(start);

    while (!stack.isEmpty()) {
      VarVersionNode node = stack.removeFirst();
      ValidationHelper.validateTrue(
        node.phantomParentNode == null && node.phantomNode == null,
        "`areVarsAnalogous` should not be called after ppmm or operator assignments resugaring");

      if (visited.contains(node)) {
        continue;
      }

      visited.add(node);
      VarVersionNode analog = this.nodes.getWithKey(new VarVersionPair(varCheck, node.version));

      if (analog == null) {
        return false;
      }

      if (node.successors.size() != analog.successors.size()) {
        return false;
      }

      // FIXME: better checking
      for (VarVersionNode dest : node.successors) {
        stack.add(dest);

        VarVersionNode sucAnalog = this.nodes.getWithKey(new VarVersionPair(varCheck, dest.version));

        if (sucAnalog == null) {
          return false;
        }
      }
    }

    return true;
  }

  private static List<VarVersionNode> getReversedPostOrder(Collection<VarVersionNode> roots) {
    List<VarVersionNode> lst = new ArrayList<>();
    Set<VarVersionNode> setVisited = new HashSet<>();
    Deque<VarVersionNode> stackNode = new ArrayDeque<>();
    Deque<Integer> stackIndex = new ArrayDeque<>();
    List<VarVersionNode> lstSuccs = new ArrayList<>();

    for (VarVersionNode root : roots) {
      List<VarVersionNode> lstTemp = new ArrayList<>();
      addToReversePostOrderListIterative(root, lstTemp, setVisited, stackNode, stackIndex, lstSuccs);
      lst.addAll(lstTemp);
    }

    return lst;
  }

  private static void addToReversePostOrderListIterative(
    VarVersionNode root,
    List<? super VarVersionNode> lst,
    Set<? super VarVersionNode> setVisited,
    Deque<VarVersionNode> stackNode,
    Deque<Integer> stackIndex,
    List<VarVersionNode> lstSuccs
  ) {
    stackNode.clear();
    stackIndex.clear();

    stackNode.add(root);
    stackIndex.add(0);

    while (!stackNode.isEmpty()) {
      VarVersionNode node = stackNode.peekLast();
      int index = stackIndex.removeLast();

      setVisited.add(node);

      lstSuccs.clear();
      lstSuccs.addAll(node.successors);
      for (; index < lstSuccs.size(); index++) {
        VarVersionNode succ = lstSuccs.get(index);

        if (!setVisited.contains(succ)) {
          stackIndex.add(index + 1);
          stackNode.add(succ);
          stackIndex.add(0);
          break;
        }
      }

      if (index == lstSuccs.size()) {
        lst.add(0, node);
        stackNode.removeLast();
      }
    }
  }
}