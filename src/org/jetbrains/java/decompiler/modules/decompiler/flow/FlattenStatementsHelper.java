// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.flow;

import org.jetbrains.java.decompiler.api.GraphFlattener;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.util.DotExporter;
import org.jetbrains.java.decompiler.util.collections.ListStack;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;

import java.util.*;


public class FlattenStatementsHelper implements GraphFlattener {
  // statement.id, node.id(direct), node.id(continue)
  private final Map<Integer, String[]> mapDestinationNodes = new HashMap<>();

  // Edge.Type.ordinal => statement to direct node map
  @SuppressWarnings("unchecked")
  private final Map<Statement, DirectNode>[] mapDestinationNodes2 = new Map[]{new HashMap<>(), new HashMap<>(), new HashMap<>()};

  // node.id(source), statement.id(destination), edge type
  @Deprecated(forRemoval = true)
  private final List<Edge> continueEdges = new ArrayList<>();

  // positive if branches
  private final Map<String, Integer> mapPosIfBranch = new HashMap<>();

  private final ListStack<List<DirectNode>> tryNodesStack = new ListStack<>();
  private final ListStack<DirectNode> finallyNodesStack = new ListStack<>();

  private DirectGraph graph;

  private RootStatement root;

  public DirectGraph buildDirectGraph(RootStatement root) {

    this.root = root;

    this.graph = new DirectGraph();

    this.graph.first = this.flattenStatement();

    // dummy exit node
    Statement dummyexit = root.getDummyExit();
    DirectNode node = this.createDirectNode(dummyexit);
    this.addDestination(dummyexit, node);

    this.setEdges();

    this.graph.sortReversePostOrder();

    this.graph.mapDestinationNodes.putAll(this.mapDestinationNodes);

    return this.graph;
  }

  private void addDestination(Statement stat, DirectNode node) {
    this.addDestination(stat, node, Edge.Type.REGULAR);
  }

  private void addDestination(Statement stat, DirectNode node, Edge.Type type) {
    this.mapDestinationNodes2[type.ordinal()].put(stat, node);
    switch (type) {
      case REGULAR:
        this.mapDestinationNodes.put(stat.id, new String[]{node.id, null});
        break;
      case CONTINUE:
        this.mapDestinationNodes.get(stat.id)[1] = node.id;
        break;
      case ALTERNATIVE:
        this.mapDestinationNodes.put(-stat.id, new String[]{node.id, null});
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

  private DirectNode createDirectNode(Statement stat, DirectNodeType type) {
    DirectNode node = DirectNode.forStat(type, stat, this.finallyNodesStack.isEmpty() ? null : this.finallyNodesStack.peek());
    this.graph.nodes.addWithKey(node, node.id);

    if (!this.tryNodesStack.isEmpty()) {
      this.tryNodesStack.peek().add(node);
      // the try itself will put all nodes in the next try too
    }

    return node;
  }

  private DirectNode flattenStatement() {
    return this.flattenStatement(this.root);
  }

  private DirectNode flattenStatement(Statement stat) {
    List<StatEdge> lstSuccEdges = new ArrayList<>();
    DirectNode sourceNode = null;
    DirectNode destinationNode = null;

    {
      switch (stat.type) {
        case BASIC_BLOCK: {
          DirectNode node = this.createDirectNode(stat);
          this.addDestination(stat, node);
          destinationNode = node;
          if (stat.getExprents() != null) {
            node.exprents = stat.getExprents();
          }

          lstSuccEdges.addAll(stat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL));
          sourceNode = node;

          // 'if' statement: record positive branch
          if (stat.getLastBasicType() == Statement.LastBasicType.IF) {
            if (lstSuccEdges.isEmpty()) {
              throw new IllegalStateException("Empty successor list for node " + sourceNode.id);
            }

            this.mapPosIfBranch.put(sourceNode.id, lstSuccEdges.get(0).getDestination().id);
          }

          List<StatEdge> basicPreds = stat.getAllPredecessorEdges();

          // TODO: sourcenode instead of stat.id?
          if (basicPreds.size() == 1) {
            StatEdge predEdge = basicPreds.get(0);

            // Look if this basic block is the successor of a sequence,
            // and connect the sequence to the block if so
            // TODO: should this be done in the sequence handling instead?
            // TODO: what is this for?
            // TODO: why is it adding ad edge from the start of the sequence to the block?
            // TODO: why is it using the basic head id, instead of the sequence id?
            // TODO: disabling this seems to not cause any validation errors, or changes in output,
            //       even though it is being called

            if (predEdge.getType() == StatEdge.TYPE_REGULAR) {
              if (predEdge.getSource() instanceof SequenceStatement) {
                this.addEdgeIfPossible(predEdge.getSource().getBasichead().id, stat);
              }
            }
          }
          break;
        }
        case CATCH_ALL:
        case TRY_CATCH: { // TODO: should these 2 be merged into 1 class?
          DirectNode node = this.createDirectNode(stat, DirectNodeType.TRY);
          this.addDestination(stat, node);
          destinationNode = node;

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

          ValidationHelper.assertTrue(tryNodes == this.tryNodesStack.pop(), "tryNodesStack is broken");
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
            ValidationHelper.assertTrue(
              finallyNode.statement == stat && finallyNode.type == DirectNodeType.FINALLY,
              "stackFinally is broken");
            combinedCatchNode.addSuccessor(DirectEdge.of(combinedCatchNode, finallyNode));

            this.finallyNodesStack.push(this.createDirectNode(stat, DirectNodeType.FINALLY_END));
            DirectNode finallyBlockNode = this.flattenStatement(st);
            finallyNode.addSuccessor(DirectEdge.of(finallyNode, finallyBlockNode));

            DirectNode finallyEndNode = this.finallyNodesStack.pop();
            ValidationHelper.assertTrue(
              finallyEndNode.statement == stat && finallyEndNode.type == DirectNodeType.FINALLY_END,
              "stackFinally is broken");

            sourceNode = finallyEndNode;
            lstSuccEdges = stat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL);
          }

          break;
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

                  this.addEdgeIfPossible(prededge.getSource().id, dest);

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

          DoStatement dostat = (DoStatement) stat;
          DoStatement.Type looptype = dostat.getLooptype();

          if (looptype == DoStatement.Type.INFINITE) {
            this.addDestination(stat, body);
            destinationNode = body;
            this.addDestination(stat, body, Edge.Type.CONTINUE);
            break;
          }

          lstSuccEdges.add(stat.getFirstSuccessor());  // exactly one edge

          switch (looptype) {
            case WHILE:
            case DO_WHILE: {
              DirectNode conditionNode = this.createDirectNode(stat, DirectNodeType.CONDITION);
              conditionNode.exprents = dostat.getConditionExprentList();

              conditionNode.addSuccessor(DirectEdge.of(conditionNode, body));

              if (looptype == DoStatement.Type.WHILE) {
                this.addDestination(stat, conditionNode); // for a while, the start is the condition
                destinationNode = conditionNode;
                this.addDestination(stat, conditionNode, Edge.Type.CONTINUE);
              } else {
                this.addDestination(stat, body); // for a do-while, the start is the body
                destinationNode = body;
                this.addDestination(stat, conditionNode, Edge.Type.CONTINUE);

                boolean found = false;
                for (Edge edge : this.continueEdges) {
                  if (edge.statid.equals(stat.id) && edge.edgetype == StatEdge.TYPE_CONTINUE) {
                    found = true;
                    break;
                  }
                }
                if (!found) {
                  // if no continue edge was found, add one from the body
                  // TODO: isn't there a better way? also, why is this only an issue for some of the while types?

                  // listEdge target: known (node)
                  this.continueEdges.add(new Edge(body, stat, Edge.Type.CONTINUE));
                }
              }
              sourceNode = conditionNode;
              break;
            }
            case FOR: {
              DirectNode initNode = this.createDirectNode(stat, DirectNodeType.INIT);
              if (dostat.getInitExprent() != null) {
                initNode.exprents = dostat.getInitExprentList();
              }

              DirectNode conditionNode = this.createDirectNode(stat, DirectNodeType.CONDITION);
              conditionNode.exprents = dostat.getConditionExprentList();

              DirectNode incrementNode = this.createDirectNode(stat, DirectNodeType.INCREMENT);
              incrementNode.exprents = dostat.getIncExprentList();

              this.addDestination(stat, initNode); // for a for, the start is the init
              destinationNode = initNode;
              this.addDestination(stat, incrementNode, Edge.Type.CONTINUE); // target for all continue edges

              conditionNode.addSuccessor(DirectEdge.of(conditionNode, body));
              initNode.addSuccessor(DirectEdge.of(initNode, conditionNode));
              incrementNode.addSuccessor(DirectEdge.of(incrementNode, conditionNode));

              boolean found = false;
              for (Edge edge : this.continueEdges) {
                if (edge.statid.equals(stat.id) && edge.edgetype == StatEdge.TYPE_CONTINUE) {
                  found = true;
                  break;
                }
              }

              if (!found) {
                // if no continue edge was found, add one from the body
                // TODO: isn't there a better way?

                // listEdge target: known (incNode)
                this.continueEdges.add(new Edge(body, stat, Edge.Type.CONTINUE));
              }

              sourceNode = conditionNode;
              break;
            }
            case FOR_EACH: {
              // for (init : inc)
              //
              // is essentially
              //
              // for (inc; ; init)
              // TODO: that ordering does not make sense

              DirectNode inc = this.createDirectNode(stat, DirectNodeType.INCREMENT);
              inc.exprents = dostat.getIncExprentList();

              // Init is foreach variable definition
              DirectNode init = this.createDirectNode(stat, DirectNodeType.FOREACH_VARDEF);
              init.exprents = dostat.getInitExprentList();

              this.addDestination(stat, inc);
              destinationNode = inc;
              this.addDestination(stat, init, Edge.Type.CONTINUE); // target for all continue edges

              init.addSuccessor(DirectEdge.of(init, body));
              inc.addSuccessor(DirectEdge.of(inc, init));

              boolean found = false;
              for (Edge edge : this.continueEdges) {
                if (edge.statid.equals(stat.id) && edge.edgetype == StatEdge.TYPE_CONTINUE) {
                  found = true;
                  break;
                }
              }

              if (!found) {
                // if no continue edge was found, add one from the body
                // TODO: isn't there a better way?

                // listEdge target: known (init)
                this.continueEdges.add(new Edge(body, stat, Edge.Type.CONTINUE));
              }

              sourceNode = init;
              break;
            }
          }
          break;
        }
        case SYNCHRONIZED:
        case SWITCH:
        case IF: {
          int statsize = stat.getStats().size();
          if (stat instanceof SynchronizedStatement) {
            statsize = 2;  // exclude the handler if synchronized
          }

          List<Exprent> tailexprlst;

          switch (stat.type) {
            case SYNCHRONIZED:
              tailexprlst = ((SynchronizedStatement) stat).getHeadexprentList();
              break;
            case SWITCH:
              tailexprlst = ((SwitchStatement) stat).getHeadexprentList();
              break;
            case IF:
              tailexprlst = ((IfStatement) stat).getHeadexprentList();
              break;
            default:
              throw new RuntimeException("Unexpected statement type: " + stat.type);
          }

          Statement first = stat.getFirst();
          DirectNode firstNode = this.createDirectNode(first);
          DirectNode outNode = firstNode;
          this.addDestination(first, firstNode);
          if (first.getExprents() != null) {
            firstNode.exprents = first.getExprents();
          }

          List<StatEdge> edges = first.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL);

          if (tailexprlst != null && tailexprlst.get(0) != null) {
            DirectNode tail = this.createDirectNode(stat, DirectNodeType.TAIL);
            tail.exprents = tailexprlst;

            firstNode.addSuccessor(DirectEdge.of(firstNode, tail));

            outNode = tail;
          }

          // 'if' statement: record positive branch
          if (stat instanceof IfStatement) {
            this.mapPosIfBranch.put(outNode.id, ((IfStatement) stat).getIfEdge().getDestination().id);
          }

          {
            List<StatEdge> basicPreds = stat.getAllPredecessorEdges();

            // TODO: sourcenode instead of stat.id?
            if (basicPreds.size() == 1) {
              StatEdge predEdge = basicPreds.get(0);

              // Look if this basic block is the successor of a sequence,
              // and connect the sequence to the block if so
              // TODO: should this be done in the sequence handling instead?
              // TODO: what is this for?
              // TODO: why is it adding ad edge from the start of the sequence to the block?
              // TODO: why is it using the basic head id, instead of the sequence id?
              // TODO: currently never called

              if (predEdge.getType() == StatEdge.TYPE_REGULAR) {
                if (predEdge.getSource() instanceof SequenceStatement) {
                  this.addEdgeIfPossible(predEdge.getSource().getBasichead().id, stat);
                }
              }
            }
          }


          for (int i = 1; i < statsize; i++) {
            this.flattenStatement(stat.getStats().get(i));
          }

          this.addDestination(stat, firstNode);
          destinationNode = firstNode;

          // Try to intercept the edges leaving the switch head and replace with relevant case nodes
          if (stat instanceof SwitchStatement) {
            SwitchStatement switchSt = (SwitchStatement) stat;

            List<List<StatEdge>> caseEdges = switchSt.getCaseEdges();
            List<List<Exprent>> caseValues = switchSt.getCaseValues();
            List<Statement> caseStatements = switchSt.getCaseStatements();

            if (caseEdges.size() != caseValues.size()) {
              if (caseEdges.size() + 1 != caseValues.size() || caseValues.get(caseValues.size() - 1).size() != 1 || caseValues.get(caseValues.size() - 1).get(0) != null) {
                throw new RuntimeException("Case edges and case values do not match");
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
              DirectNode caseNode = this.createDirectNode(thisCaseStatement, DirectNodeType.CASE);
              caseNode.exprents = finalVals;

              outNode.addSuccessor(DirectEdge.of(outNode, caseNode));

              this.handleFinally(thisCaseEdges, caseNode);
            }

            if (caseEdges.size() < caseValues.size()) {
              // default case
              List<StatEdge> thisCaseEdges = new ArrayList<>(1);
              thisCaseEdges.add(switchSt.getDefaultEdge());

              this.handleFinally(thisCaseEdges, outNode);
            }
          } else {
            // add edges
            this.handleFinally(edges, outNode);
          }

          if (stat instanceof IfStatement && ((IfStatement) stat).iftype == IfStatement.IFTYPE_IF && !stat.getAllSuccessorEdges().isEmpty()) {
            lstSuccEdges.add(stat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL).get(0));  // exactly one edge
            sourceNode = outNode;
          }

          // Adds an edge from the last if statement to the current if statement, if the current if statement's head statement has no predecessor
          // This was made to mask a failure in EliminateLoopsHelper and isn't used currently (over the current test set) but could theoretically still happen!
          // TODO: what?
          // TODO: use java code gen to generate a test for this?
          if (stat instanceof IfStatement && ((IfStatement) stat).iftype == IfStatement.IFTYPE_IF && !stat.getPredecessorEdges(StatEdge.TYPE_REGULAR).isEmpty()) {
            if (stat.getFirst().getPredecessorEdges(StatEdge.TYPE_REGULAR).isEmpty()) {
              StatEdge edge = stat.getPredecessorEdges(StatEdge.TYPE_REGULAR).get(0);

              Statement source = edge.getSource();
              if (source instanceof IfStatement && ((IfStatement) source).iftype == IfStatement.IFTYPE_IF && !source.getAllSuccessorEdges().isEmpty()) {
                DirectNode srcnd = this.graph.nodes.getWithKey(source.getFirst().id + "_tail");

                if (srcnd != null) {
                  // old ifstat->head
                  Edge newEdge = new Edge(srcnd, stat, edge.getType() == StatEdge.TYPE_CONTINUE ? Edge.Type.CONTINUE : Edge.Type.REGULAR);

                  // Add if it doesn't exist already
                  if (!this.continueEdges.contains(newEdge)) {
                    this.continueEdges.add(newEdge);
                  }
                }
              }
            }
          }
          break;
        }
        case SEQUENCE:
        case ROOT: {
          int statsize = stat.getStats().size();

          DirectNode firstBlock = this.flattenStatement(stat.getFirst());

          for (int i = 1; i < statsize; i++) {
            this.flattenStatement(stat.getStats().get(i));
          }

          this.addDestination(stat, firstBlock);
          destinationNode = firstBlock;

          break;
        }
      }
    }

    if (sourceNode != null) {
      this.handleFinally(lstSuccEdges, sourceNode);
    }

    return destinationNode;
  }

  private void handleFinally(List<StatEdge> lstSuccEdges, DirectNode sourcenode) {
    for (StatEdge edge : lstSuccEdges) {
      int edgetype = edge.getType();
      Statement destination = edge.getDestination();
      this.saveEdge(sourcenode, destination, edgetype, edge.closure);
    }
  }


  private void saveEdge(DirectNode sourcenode, Statement destination, int edgetype, Statement closure) {
    if (closure instanceof CatchAllStatement && ((CatchAllStatement) closure).isFinally()) {
      if (edgetype == StatEdge.TYPE_FINALLYEXIT) {
        DirectNode dest = this.finallyNodesStack.peek();
        if (dest.statement == closure) {
          ValidationHelper.assertTrue(dest.type == DirectNodeType.FINALLY_END, "Finally destination mismatch");
          sourcenode.addSuccessor(DirectEdge.of(sourcenode, dest));
        } else {
          // FIXME: finally exits often point wrong
          ValidationHelper.assertTrue(dest.type == DirectNodeType.FINALLY_END, "Finally destination mismatch");
          sourcenode.addSuccessor(DirectEdge.of(sourcenode, dest));
        }
      }
//      else if (edgetype == StatEdge.TYPE_BREAK) {
//        DirectNode dest = this.finallyNodesStack.peek();
//        ValidationHelper.assertTrue(dest.statement == closure, "Finally destination mismatch");
//        // ValidationHelper.assertTrue(dest.type == DirectNodeType.FINALLY, "Finally destination mismatch");
//        sourcenode.addSuccessor(DirectEdge.of(sourcenode, dest));
//      }
//        else {
//        ValidationHelper.assertTrue(false, "Unexpected finally edge type");
//      }
      else {
        this.continueEdges.add(new Edge(sourcenode.id, destination.id, edgetype));
      }
    } else  /* if (edgetype != StatEdge.TYPE_FINALLYEXIT) */ {
      this.continueEdges.add(new Edge(sourcenode.id, destination.id, edgetype));
    }
  }

  private void addEdgeIfPossible(Integer predEdge, Statement stat) {
    String[] lastbasicdests = this.mapDestinationNodes.get(predEdge);

    if (lastbasicdests != null) {
      this.continueEdges.add(new Edge(this.graph.nodes.getWithKey(lastbasicdests[0]).id, stat.id, StatEdge.TYPE_REGULAR));
    }
  }

  private void setEdges() {

    for (Edge edge : this.continueEdges) {

      String sourceid = edge.sourceid;
      Integer statid = edge.statid;

      DirectNode source = edge.source != null ? edge.source : this.graph.nodes.getWithKey(sourceid);

      String[] strings = this.mapDestinationNodes.get(statid);
      if (strings == null) {
        DotExporter.toDotFile(this.graph, this.root.mt, "errorDGraph");

        throw new IllegalStateException("Could not find destination nodes for stat id " + statid + " from source " + sourceid);
      }
      // TODO: continue edge type?
      DirectNode dest = this.graph.nodes.getWithKey(strings[edge.edgetype == StatEdge.TYPE_CONTINUE ? 1 : 0]);

      if (edge.edgetype == StatEdge.TYPE_FINALLYEXIT) {
        if (source.tryFinally != null &&
            source.tryFinally.type == DirectNodeType.FINALLY_END /*&&
            source.tryFinally.tryFinally == dest.tryFinally*/) { // dest is always the dummy exit
          dest = source.tryFinally;
        } else {
          // Should only happen if there are unprocessed finallies, should look into a check for that
          // TODO: finally edges only exist in the graph if there is a finally block, so this should never happen??

          continue;
        }
      }

      DirectEdge diedge = edge.edgetype == StatEdge.TYPE_EXCEPTION ? DirectEdge.exception(source, dest) : DirectEdge.of(source, dest);

      source.addSuccessor(diedge);

      if (this.mapPosIfBranch.containsKey(sourceid) && !statid.equals(this.mapPosIfBranch.get(sourceid))) {
        this.graph.mapNegIfBranch.put(sourceid, dest.id);
      }
    }

    for (int i = 0; i < 2; i++) {
//      for (Entry<String, List<String[]>> ent : (i == 0 ? this.mapShortRangeFinallyPathIds : this.mapLongRangeFinallyPathIds).entrySet()) {
//
//        List<FinallyPathWrapper> newLst = new ArrayList<>();
//
//        List<String[]> lst = ent.getValue();
//        for (String[] arr : lst) {
//
//          boolean isContinueEdge = arr[i == 0 ? 4 : 3] != null;
//
//          DirectNode dest = this.graph.nodes.getWithKey(this.mapDestinationNodes.get(Integer.parseInt(arr[1]))[isContinueEdge ? 1 : 0]);
//          DirectNode enter = this.graph.nodes.getWithKey(this.mapDestinationNodes.get(Integer.parseInt(arr[2]))[0]);
//
//          newLst.add(new FinallyPathWrapper(arr[0], dest.id, enter.id));
//
//          if (i == 0 && arr[3] != null) {
//            this.graph.mapFinallyMonitorExceptionPathExits.put(ent.getKey(), dest.id);
//          }
//        }
//
//        if (!newLst.isEmpty()) {
//          (i == 0 ? this.graph.mapShortRangeFinallyPaths : this.graph.mapLongRangeFinallyPaths).put(ent.getKey(), new ArrayList<>(new HashSet<>(newLst)));
//        }
//      }
    }
  }

  public Map<Integer, String[]> getMapDestinationNodes() {
    return this.mapDestinationNodes;
  }

  public static final class FinallyPathWrapper {
    public final String source;
    public final String destination;
    public final String entry;

    private FinallyPathWrapper(String source, String destination, String entry) {
      this.source = source;
      this.destination = destination;
      this.entry = entry;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof FinallyPathWrapper)) {
        return false;
      }

      FinallyPathWrapper fpw = (FinallyPathWrapper) o;
      return (this.source + ":" + this.destination + ":" + this.entry).equals(fpw.source + ":" + fpw.destination + ":" + fpw.entry);
    }

    @Override
    public int hashCode() {
      return (this.source + ":" + this.destination + ":" + this.entry).hashCode();
    }

    @Override
    public String toString() {
      return this.source + "->(" + this.entry + ")->" + this.destination;
    }
  }


  private static class StackEntry {

    public final CatchAllStatement catchstatement;
    public final boolean state;

    StackEntry(CatchAllStatement catchstatement, boolean state) {

      this.catchstatement = catchstatement;
      this.state = state;
    }
  }

  private static class Edge {
    @Deprecated
    public final String sourceid;
    public DirectNode source;
    @Deprecated
    public final Integer statid;
    public Statement dest;
    public final int edgetype;

    @Deprecated
    Edge(String sourceid, Integer statid, int edgetype) {
      this.sourceid = sourceid;
      this.statid = statid;
      this.edgetype = edgetype;
    }

    Edge(DirectNode source, Statement dest, Type edgetype) {
      this.source = source;
      this.sourceid = source.id;
      this.dest = dest;
      switch (edgetype) {
        case REGULAR:
          this.statid = dest.id;
          this.edgetype = StatEdge.TYPE_REGULAR;
          break;
        case CONTINUE:
          this.statid = dest.id;
          this.edgetype = StatEdge.TYPE_CONTINUE;
          break;
        case ALTERNATIVE:
          this.statid = -dest.id;
          this.edgetype = StatEdge.TYPE_REGULAR;
          break;
        case EXCEPTION:
          this.statid = dest.id;
          this.edgetype = StatEdge.TYPE_EXCEPTION;
          break;
        case FINALLY_EXIT:
          this.statid = dest.id;
          this.edgetype = StatEdge.TYPE_FINALLYEXIT;
          break;
        default:
          throw new RuntimeException("Unknown edge type: " + edgetype);
      }
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
      return this.edgetype == edge.edgetype && Objects.equals(this.sourceid, edge.sourceid) && Objects.equals(this.statid, edge.statid);
    }

    @Override
    public String toString() {
      return "Source: " + this.sourceid + " Stat: " + this.statid + " Edge: " + this.edgetype;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.sourceid, this.statid, this.edgetype);
    }

    enum Type {
      REGULAR, CONTINUE, ALTERNATIVE, EXCEPTION, FINALLY_EXIT
    }
  }
}
