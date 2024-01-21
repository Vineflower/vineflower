// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.flow;

import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.util.DotExporter;
import org.jetbrains.java.decompiler.util.collections.ListStack;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;

import java.util.*;


public class FlattenStatementsHelper {
  // statement to direct node map
  private final Map<Statement, DirectNode> mapRegularDestinationNodes = new HashMap<>();
  private final Map<Statement, DirectNode> mapContinueDestinationNodes = new HashMap<>();

  // Lazy edges
  private final List<Edge> indirectEdges = new ArrayList<>();

  // Positive if branches
  private final Map<DirectNode, Statement> mapPosIfBranch = new HashMap<>();

  private final ListStack<List<DirectNode>> tryNodesStack = new ListStack<>();
  private final ListStack<DirectNode> finallyNodesStack = new ListStack<>();

  private DirectGraph graph;

  private RootStatement root;

  public static DirectGraph build(RootStatement root) {
    return new FlattenStatementsHelper().buildDirectGraph(root);
  }

  public DirectGraph buildDirectGraph(RootStatement root) {

    this.root = root;

    this.graph = new DirectGraph();

    this.graph.first = this.flattenStatement(root);

    // dummy exit node
    Statement dummyexit = root.getDummyExit();
    DirectNode node = this.createDirectNode(dummyexit);
    this.addDestination(dummyexit, node);

    this.setEdges();

    this.graph.sortReversePostOrder();

    return this.graph;
  }

  private void addDestination(Statement stat, DirectNode node) {
    this.addDestination(stat, node, Edge.Type.REGULAR);
  }

  private void addDestination(Statement stat, DirectNode node, Edge.Type type) {
    switch (type) {
      case REGULAR:
        this.mapRegularDestinationNodes.put(stat, node);
        break;
      case CONTINUE:
        this.mapContinueDestinationNodes.put(stat, node);
        break;
      default:
        throw new RuntimeException("Unexpected edge type: " + type);
    }
  }

  private DirectNode createDirectNode(Statement stat) {
    final DirectNode directNode = this.createDirectNode(stat, DirectNodeType.DIRECT);
    if (stat instanceof BasicBlockStatement) {
      directNode.block = (BasicBlockStatement) stat;
    }

    return directNode;
  }

  private DirectNode createDirectNode(Statement stat, List<Exprent> exprents) {
    DirectNode node = this.createDirectNode(stat);
    if (exprents != null) {
      node.exprents = exprents;
    }
    return node;
  }

  private DirectNode createDirectNode(Statement stat, DirectNodeType type) {
    DirectNode node = DirectNode.forStat(type, stat, this.finallyNodesStack.isEmpty() ? null : this.finallyNodesStack.peek());
    this.graph.nodes.addWithKey(node, node.id);

    if (!this.tryNodesStack.isEmpty()) {
      this.tryNodesStack.peek().add(node);
      // the try itself will put all nodes in the next try too
    }

    return node;
  }

  private DirectNode createDirectNode(Statement stat, DirectNodeType type, List<Exprent> exprents) {
    DirectNode node = this.createDirectNode(stat, type);
    if (exprents != null) {
      node.exprents = exprents;
    }
    return node;
  }

  private DirectNode flattenStatement(Statement stat) {
    switch (stat.type) {
      case BASIC_BLOCK: {
        DirectNode node = this.createDirectNode(stat);
        this.addDestination(stat, node);
        if (stat.getExprents() != null) {
          node.exprents = stat.getExprents();
        }

        // 'if' statement: record positive branch
        if (stat.getLastBasicType() == Statement.LastBasicType.IF) {
          if (!stat.hasAnyDirectSuccessor()) {
            throw new IllegalStateException("Empty successor list for node " + node.id);
          }

          this.mapPosIfBranch.put(node, stat.getFirstDirectSuccessor().getDestination());
        }

        List<StatEdge> basicPreds = stat.getAllPredecessorEdges();

        // TODO: sourcenode instead of stat.id?
        if (basicPreds.size() == 1) {
          StatEdge predEdge = basicPreds.get(0);

          // Look if this basic block is the successor of a sequence,
          // and connect the sequence to the block if so
          // TODO: should this be done in the sequence handling instead?
          //   what is this for?
          //   why is it adding ad edge from the start of the sequence to the block?
          //   why is it using the basic head id, instead of the sequence id?
          //   disabling this seems to not cause any validation errors, or changes in output,
          //       even though it is being called

          if (predEdge.getType() == StatEdge.TYPE_REGULAR) {
            if (predEdge.getSource() instanceof SequenceStatement) {
              this.addEdgeIfPossible(predEdge.getSource().getBasichead(), stat);
            }
          }
        }

        this.addEdges(node, stat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL));
        return node;
      }
      case CATCH_ALL:
      case TRY_CATCH: { // TODO: should these 2 paths be split?
        DirectNode node = this.createDirectNode(stat, DirectNodeType.TRY);
        this.addDestination(stat, node);

        boolean isFinally = stat instanceof CatchAllStatement && ((CatchAllStatement) stat).isFinally();

        VBStyleCollection<Statement, Integer> stats = stat.getStats();

        int endCatchIndex = isFinally ? stats.size() - 1 : stats.size();

        if (stat instanceof CatchStatement) {
          CatchStatement catchStat = (CatchStatement) stat;
          List<Exprent> resources = catchStat.getResources();
          if (!resources.isEmpty()) {
            node.exprents = resources;
          }
        }


        if (isFinally) {
          this.finallyNodesStack.push(this.createDirectNode(stat, DirectNodeType.FINALLY));
        }

        List<DirectNode> tryNodes = new ArrayList<>();
        this.tryNodesStack.add(tryNodes);

        DirectNode tryBlock = this.flattenStatement(stat.getFirst());
        node.addSuccessor(DirectEdge.of(node, tryBlock));

        ValidationHelper.validateTrue(tryNodes == this.tryNodesStack.pop(), "tryNodesStack is broken");
        if (!this.tryNodesStack.isEmpty()) {
          this.tryNodesStack.peek().addAll(tryNodes);
        }

        DirectNode combinedCatchNode = this.createDirectNode(stat, DirectNodeType.COMBINED_CATCH);

        for (DirectNode innerTryNode : tryNodes) {
          innerTryNode.addSuccessor(DirectEdge.exception(innerTryNode, combinedCatchNode));
        }

        // TODO: should this be an exception edge for catch blocks?
        node.addSuccessor(DirectEdge.of(node, combinedCatchNode));

        // catch blocks
        for (int i = 1; i < endCatchIndex; i++) {
          Statement st = stats.get(i);

          // TODO: add catch variable to this node
          DirectNode catchNode = this.createDirectNode(st, DirectNodeType.CATCH);
          DirectNode handlerNode = this.flattenStatement(st);

          combinedCatchNode.addSuccessor(DirectEdge.of(combinedCatchNode, catchNode));
          catchNode.addSuccessor(DirectEdge.of(catchNode, handlerNode));
        }

        if (isFinally) {

          Statement st = stats.get(endCatchIndex);
          DirectNode finallyNode = this.finallyNodesStack.pop();
          ValidationHelper.validateTrue(
            finallyNode.statement == stat && finallyNode.type == DirectNodeType.FINALLY,
            "stackFinally is broken");
          combinedCatchNode.addSuccessor(DirectEdge.of(combinedCatchNode, finallyNode));

          this.finallyNodesStack.push(this.createDirectNode(stat, DirectNodeType.FINALLY_END));
          DirectNode finallyBlockNode = this.flattenStatement(st);
          finallyNode.addSuccessor(DirectEdge.of(finallyNode, finallyBlockNode));

          DirectNode finallyEndNode = this.finallyNodesStack.pop();
          ValidationHelper.validateTrue(
            finallyEndNode.statement == stat && finallyEndNode.type == DirectNodeType.FINALLY_END,
            "stackFinally is broken");
        }

        return node;
      }
      case DO: {
        if (!stat.hasBasicSuccEdge()) { // infinite loop TODO: why no just check the loop type?
          if (stat.hasSuccessor(StatEdge.TYPE_REGULAR)) {
            Statement dest = stat.getSuccessorEdges(StatEdge.TYPE_REGULAR).get(0).getDestination();

            if (dest.getAllPredecessorEdges().size() == 1) {
              // If the successor only has one backedge, it is the current loop
              List<StatEdge> prededges = stat.getPredecessorEdges(StatEdge.TYPE_REGULAR);

              if (!prededges.isEmpty()) {
                StatEdge prededge = prededges.get(0);

                // Find destinations of loop's predecessor

                this.addEdgeIfPossible(prededge.getSource(), dest);

                // Note: It seems that for infinite loops, the loop's predecessor gets an extra edge
                // to the loop's "destination" (usually the place the loop breaks to).
                // TODO: This feels wrong. EDIT: seems to try to "fix"
                //       a finally processing bug, consider this a temporary
                //       bandaid
              }
            }
          }
        }

        DirectNode body = this.flattenStatement(stat.getFirst());

        DoStatement doStat = (DoStatement) stat;
        DoStatement.Type loopType = doStat.getLooptype();

        switch (loopType) {
          case INFINITE: {
            this.addDestination(stat, body);
            this.addDestination(stat, body, Edge.Type.CONTINUE);
            return body;
          }
          case WHILE: {
            DirectNode conditionNode = this.createDirectNode(stat, DirectNodeType.CONDITION, doStat.getConditionExprentList());

            conditionNode.addSuccessor(DirectEdge.of(conditionNode, body));

            this.addDestination(stat, conditionNode); // for a while, the start is the condition
            this.addDestination(stat, conditionNode, Edge.Type.CONTINUE); // for a while, continues go to the condition

            this.addEdge(conditionNode, stat.getFirstSuccessor());
            return conditionNode;
          }
          case DO_WHILE: {
            DirectNode conditionNode = this.createDirectNode(stat, DirectNodeType.CONDITION, doStat.getConditionExprentList());

            conditionNode.addSuccessor(DirectEdge.of(conditionNode, body));

            this.addDestination(stat, body); // for a do-while, the start is the body
            this.addDestination(stat, conditionNode, Edge.Type.CONTINUE); // for a do-while, continues go to the condition

            this.handleLoopEnd(stat, body);

            this.addEdge(conditionNode, stat.getFirstSuccessor());
            return body;
          }
          case FOR: {
            DirectNode initNode = this.createDirectNode(stat, DirectNodeType.INIT);
            if (doStat.getInitExprent() != null) {
              initNode.exprents = doStat.getInitExprentList();
            }

            DirectNode conditionNode = this.createDirectNode(stat, DirectNodeType.CONDITION, doStat.getConditionExprentList());
            DirectNode incrementNode = this.createDirectNode(stat, DirectNodeType.INCREMENT, doStat.getIncExprentList());

            this.addDestination(stat, initNode); // for a for, the start is the init
            this.addDestination(stat, incrementNode, Edge.Type.CONTINUE); // target for all continue edges

            conditionNode.addSuccessor(DirectEdge.of(conditionNode, body));
            initNode.addSuccessor(DirectEdge.of(initNode, conditionNode));
            incrementNode.addSuccessor(DirectEdge.of(incrementNode, conditionNode));

            this.handleLoopEnd(stat, body);

            this.addEdge(conditionNode, stat.getFirstSuccessor());
            return initNode;
          }
          case FOR_EACH: {
            // for (init : inc)
            //
            // is essentially
            //
            // for (inc; ; init)
            // TODO: that ordering does not make sense

            DirectNode inc = this.createDirectNode(stat, DirectNodeType.INCREMENT, doStat.getIncExprentList());

            // Init is foreach variable definition
            DirectNode init = this.createDirectNode(stat, DirectNodeType.FOREACH_VARDEF, doStat.getInitExprentList());

            this.addDestination(stat, inc);
            this.addDestination(stat, init, Edge.Type.CONTINUE); // target for all continue edges

            init.addSuccessor(DirectEdge.of(init, body));
            inc.addSuccessor(DirectEdge.of(inc, init));

            this.handleLoopEnd(stat, body);

            this.addEdge(init, stat.getFirstSuccessor());
            return inc;
          }
          default: {
            throw new RuntimeException("Unknown loop type: " + loopType);
          }
        }
      }
      case SYNCHRONIZED: {
        List<Exprent> tailexprlst = ((SynchronizedStatement) stat).getHeadexprentList();

        Statement first = stat.getFirst();
        DirectNode firstNode = this.createDirectNode(first, first.getExprents());
        this.addDestination(first, firstNode);
        this.addDestination(stat, firstNode);

        if (tailexprlst != null && tailexprlst.get(0) != null) {
          DirectNode tail = this.createDirectNode(stat, DirectNodeType.TAIL, tailexprlst);
          firstNode.addSuccessor(DirectEdge.of(firstNode, tail));

          // TODO: can synchronized have multiple successors?
          this.addEdges(tail, first.getAllDirectSuccessorEdges());
        } else {
          this.addEdges(firstNode, first.getAllDirectSuccessorEdges());
        }

        this.flattenStatement(stat.getStats().get(1));

        this.handleTailedStat(stat);

        return firstNode;
      }
      case SWITCH: {
        SwitchStatement switchSt = (SwitchStatement) stat;
        List<Exprent> tailexprlst = switchSt.getHeadexprentList();

        Statement first = stat.getFirst();
        DirectNode firstNode = this.createDirectNode(first, first.getExprents());
        DirectNode outNode = firstNode;
        this.addDestination(first, firstNode);
        this.addDestination(stat, firstNode);

        if (tailexprlst != null && tailexprlst.get(0) != null) {
          DirectNode tail = this.createDirectNode(stat, DirectNodeType.TAIL, tailexprlst);

          firstNode.addSuccessor(DirectEdge.of(firstNode, tail));

          outNode = tail;
        }

        this.handleTailedStat(stat);

        for (int i = 1; i < stat.getStats().size(); i++) {
          this.flattenStatement(stat.getStats().get(i));
        }

        // Try to intercept the edges leaving the switch head and replace with relevant case nodes
        List<List<StatEdge>> caseEdges = switchSt.getCaseEdges();
        List<List<Exprent>> caseValues = switchSt.getCaseValues();
        List<Statement> caseStatements = switchSt.getCaseStatements();

        if (caseEdges.size() != caseValues.size()) {
          if (caseEdges.size() + 1 != caseValues.size() || caseValues.get(caseValues.size() - 1).size() != 1 || caseValues.get(caseValues.size() - 1).get(0) != null) {
            throw new IllegalStateException("Case edges and case values do not match");
          }
        }

        for (int i = 0; i < caseEdges.size(); i++) {

          List<Exprent> values = caseValues.get(i);
          List<StatEdge> thisCaseEdges = caseEdges.get(i);
          Statement thisCaseStatement = caseStatements.get(i);

          // Default case val can be null
          List<Exprent> finalVals = null;
          if (values != null) {
            finalVals = new ArrayList<>();
            for (Exprent value : values) {
              if (value != null) {
                finalVals.add(value);
              }
            }
          }

          // Build node out of the case exprents
          DirectNode caseNode = this.createDirectNode(thisCaseStatement, DirectNodeType.CASE, finalVals);

          outNode.addSuccessor(DirectEdge.of(outNode, caseNode));

          this.addEdges(caseNode, thisCaseEdges);
        }

        if (caseEdges.size() < caseValues.size()) {
          // default case
          this.addEdge(outNode, switchSt.getDefaultEdge());
        }

        return firstNode;
      }
      case IF: {
        IfStatement ifStat = (IfStatement) stat;

        List<Exprent> tailexprlst = ifStat.getHeadexprentList();

        Statement first = stat.getFirst();
        DirectNode firstNode = this.createDirectNode(first, first.getExprents());
        DirectNode outNode = firstNode;
        this.addDestination(first, firstNode);
        this.addDestination(stat, firstNode);

        if (tailexprlst != null && tailexprlst.get(0) != null) {
          DirectNode tail = this.createDirectNode(stat, DirectNodeType.TAIL, tailexprlst);

          firstNode.addSuccessor(DirectEdge.of(firstNode, tail));

          outNode = tail;
        }

        // 'if' statement: record positive branch
        this.mapPosIfBranch.put(outNode, ifStat.getIfEdge().getDestination());

        this.handleTailedStat(stat);

        for (int i = 1; i < stat.getStats().size(); i++) {
          this.flattenStatement(stat.getStats().get(i));
        }

        // add edges
        this.addEdges(outNode, first.getAllDirectSuccessorEdges());

        if (ifStat.iftype == IfStatement.IFTYPE_IF && stat.hasAnyDirectSuccessor()) {
          // add implicit else edge
          this.addEdge(outNode, stat.getFirstDirectSuccessor());
        }

        return firstNode;
      }
      case SEQUENCE:
      case ROOT: {

        DirectNode firstBlock = this.flattenStatement(stat.getFirst());

        int statsize = stat.getStats().size();
        for (int i = 1; i < statsize; i++) {
          this.flattenStatement(stat.getStats().get(i));
        }

        this.addDestination(stat, firstBlock);
        return firstBlock;
      }
      default: {
        throw new RuntimeException("Unexpected statement type");
      }
    }
  }

  private void handleTailedStat(Statement stat) {
    List<StatEdge> basicPreds = stat.getAllPredecessorEdges();

    // TODO: sourcenode instead of stat.id?
    if (basicPreds.size() == 1) {
      StatEdge predEdge = basicPreds.get(0);

      // Look if this basic block is the successor of a sequence,
      // and connect the sequence to the block if so
      // TODO: should this be done in the sequence handling instead?
      //   what is this for?
      //   why is it adding ad edge from the start of the sequence to the block?
      //   why is it using the basic head id, instead of the sequence id?
      //   currently never called
      //   this is the same as the basic block handling?

      if (predEdge.getType() == StatEdge.TYPE_REGULAR) {
        if (predEdge.getSource() instanceof SequenceStatement) {
          this.addEdgeIfPossible(predEdge.getSource().getBasichead(), stat);
        }
      }
    }
  }

  private void handleLoopEnd(Statement stat, DirectNode body) {
    for (Edge edge : this.indirectEdges) {
      if (edge.stat == stat && edge.type == Edge.Type.CONTINUE) {
        return;
      }
    }

    // if no continue edge was found, add one from the body
    // TODO: isn't there a better way?
    // FIXME: this feels like a hack

    // listEdge target: known (init)
    this.indirectEdges.add(new Edge(body, stat, Edge.Type.CONTINUE));
  }

  private void addEdges(DirectNode sourceNode, List<StatEdge> lstSuccEdges) {
    for (StatEdge edge : lstSuccEdges) {
      this.addEdge(sourceNode, edge);
    }
  }


  private void addEdge(DirectNode sourceNode, StatEdge edge) {
    int edgeType = edge.getType();
    Statement destination = edge.getDestination();
    this.saveEdge(sourceNode, destination, edgeType);
  }


  private void saveEdge(DirectNode sourceNode, Statement destination, int edgeType) {
    this.indirectEdges.add(new Edge(sourceNode, destination, edgeType));
  }

  private void addEdgeIfPossible(Statement predEdge, Statement stat) {
    DirectNode lastBasic = this.mapRegularDestinationNodes.get(predEdge);

    if (lastBasic != null) {
      this.indirectEdges.add(new Edge(lastBasic, stat, Edge.Type.REGULAR));
    }
  }

  private void setEdges() {
    for (Edge edge : this.indirectEdges) {
      DirectNode source = edge.source;
      DirectNode dest = this.getDestination(edge);

      if (dest == null) {
        DotExporter.toDotFile(this.graph, this.root.mt, "errorDGraph");

        throw new IllegalStateException("Could not find destination nodes for stat id " + edge.stat + " from source " + source);
      }

      if (edge.type == Edge.Type.FINALLY_EXIT) {
        if (source.tryFinally != null &&
            source.tryFinally.type == DirectNodeType.FINALLY_END) {
          dest = source.tryFinally;
        } else {
          // Should only happen if there are unprocessed finallies, should look into a check for that
          // TODO: finally edges only exist in the graph if there is a finally block, so this should never happen??
          //   think about why these do happen?
          continue;
        }
      }

      DirectEdge diedge = edge.type == Edge.Type.EXCEPTION ?
        DirectEdge.exception(source, dest) : DirectEdge.of(source, dest);

      source.addSuccessor(diedge);

      if (this.mapPosIfBranch.containsKey(source) && edge.stat != this.mapPosIfBranch.get(source)) {
        this.graph.mapNegIfBranch.put(source.id, dest.id);
      }
    }
  }

  public DirectNode getDirectNode(Statement stat) {
    return this.mapRegularDestinationNodes.get(stat);
  }


  private DirectNode getDestination(Edge edge) {
    return (edge.type == Edge.Type.CONTINUE ? this.mapContinueDestinationNodes : this.mapRegularDestinationNodes)
      .get(edge.stat);
  }


  private static class Edge {
    public DirectNode source;
    public Statement stat;
    final Type type;

    Edge(DirectNode source, Statement stat, int edgetype) {
      this.source = source;
      this.stat = stat;

      if (edgetype == StatEdge.TYPE_CONTINUE) {
        this.type = Type.CONTINUE;
      } else if (edgetype == StatEdge.TYPE_FINALLYEXIT) {
        this.type = Type.FINALLY_EXIT;
      } else if (edgetype == StatEdge.TYPE_EXCEPTION) {
        this.type = Type.EXCEPTION;
      } else {
        this.type = Type.REGULAR;
      }
    }

    Edge(DirectNode source, Statement stat, Type type) {
      this.source = source;
      this.stat = stat;
      this.type = type;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || this.getClass() != o.getClass()) {
        return false;
      }

      Edge edge = (Edge) o;
      return this.type == edge.type && Objects.equals(this.source, edge.source) && Objects.equals(this.stat, edge.stat);
    }

    @Override
    public String toString() {
      return "Source: " + this.source + " Dest: " + this.stat + " Edge: " + this.type;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.source, this.stat, this.type);
    }

    enum Type {
      REGULAR, CONTINUE, EXCEPTION, FINALLY_EXIT
    }
  }
}
