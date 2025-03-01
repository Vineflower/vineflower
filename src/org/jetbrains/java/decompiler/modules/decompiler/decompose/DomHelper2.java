// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.decompose;

import org.jetbrains.java.decompiler.api.plugin.GraphParser;
import org.jetbrains.java.decompiler.code.SwitchInstruction;
import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.code.cfg.ControlFlowGraph;
import org.jetbrains.java.decompiler.main.rels.MethodProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.LabelHelper;
import org.jetbrains.java.decompiler.modules.decompiler.SequenceHelper;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.DomBlocks.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement.EdgeDirection;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.DotExporter;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;
import org.jetbrains.java.decompiler.util.collections.fixed.FastFixedSet;
import org.jetbrains.java.decompiler.util.collections.fixed.FastFixedSetFactory;

import java.util.*;

public final class DomHelper2 implements GraphParser {

  @Override
  public RootStatement createStatement(ControlFlowGraph graph, StructMethod mt) {
    return parseGraph(graph, mt, 0);
  }

  record InitialConversionResult(
    List<DomBlock> blocks,
    DomBlock startBlock,
    Map<BasicBlock, DomBlock> simpleToDom
  ) {
  }

  private static InitialConversionResult initialConversion(ControlFlowGraph graph, StructMethod mt) {
    Map<BasicBlock, DomBlock> simpleToDom = new LinkedHashMap<>();
    List<BasicBlock> blocks = graph.getBlocks();

    for (BasicBlock block : blocks) {
      simpleToDom.put(block, new DomBasicBlock(block));
    }

    // dummy exit statement
    DomExit dummyExit = new DomExit();

    // Add all the edges
    for (var entry : simpleToDom.entrySet()) {
      var block = entry.getKey();
      var domBlock = entry.getValue();

      for (var succ : block.getSuccs()) {
        if (succ == graph.getLast()) {
          DomEdge.create(domBlock, dummyExit, null, DomEdgeType.EXIT);
          continue;
        }

        var succBlock = simpleToDom.get(succ);
        DomEdge.create(domBlock, succBlock);
      }
    }

    // Detect if and switches
    for (var entry : simpleToDom.entrySet()) {
      var block = entry.getKey();
      var domBlock = entry.getValue();

      if (block.getLastBasicType() == Statement.LastBasicType.IF) {
        if (domBlock.getSuccessors().size() != 2) {
          throw new RuntimeException("If statement with more than 2 successors detected!");
        }

        var successors = domBlock.getSuccessors().iterator();
        DomEdge ifEdge = successors.next();
        DomEdge elseEdge = successors.next();

        DomIfBlock ifBlock = new DomIfBlock((DomBasicBlock) domBlock, ifEdge, elseEdge);
        ifEdge.changeClosure(ifBlock);
        elseEdge.changeClosure(ifBlock);

        entry.setValue(ifBlock);

        for (var predEdge : new ArrayList<>(domBlock.getPredecessors())) {
          predEdge.changeDestination(ifBlock);
        }
      } else if (block.getLastBasicType() == Statement.LastBasicType.SWITCH) {
        var successors = domBlock.getSuccessors();
        var instruction = block.getLastInstruction();
        if (!(instruction instanceof SwitchInstruction switchIns)) {
          throw new RuntimeException("Switch statement without switch instruction!");
        }
        var values = switchIns.getValues();
        if (values.length + 1 != successors.size()) {
          throw new RuntimeException("Switch statement with wrong number of successors!");
        }

        var successorIterator = successors.iterator();
        List<DomEdge> normalEdges = new ArrayList<>();
        var defaultSuccessor = successorIterator.next();

        while (successorIterator.hasNext()) {
          normalEdges.add(successorIterator.next());
        }

        RawDomSwitchBlock switchBlock = new RawDomSwitchBlock((DomBasicBlock) domBlock, defaultSuccessor, normalEdges, values);
        defaultSuccessor.changeClosure(switchBlock);
        for (DomEdge edge : normalEdges) {
          edge.changeClosure(switchBlock);
        }
        entry.setValue(switchBlock);

        for (var predEdge : new ArrayList<>(domBlock.getPredecessors())) {
          predEdge.changeDestination(switchBlock);
        }
      }
    }

    // Create final list, and return
    List<DomBlock> lstDomBlocks = new ArrayList<>(simpleToDom.values());

    return new InitialConversionResult(
      lstDomBlocks,
      simpleToDom.get(graph.getFirst()),
      simpleToDom
    );
  }

  private static DomBlock solveDAG(List<DomBlock> blockList, DomBlock startBlock, DomTracer2 tracer) {
    tracer.info(blockList, "solveDAG");
    Set<DomBlock> blocks = new LinkedHashSet<>(blockList);

    // Do a toposort
    // Step 1: Gather and count the number of incoming edges for each block
    HashMap<DomBlock, List<DomEdge>> mapIncomingEdges = new HashMap<>();
    HashMap<DomBlock, List<DomEdge>> mapOutgoingEdges = new HashMap<>();
    for (DomBlock block : blocks) {
      for (DomEdge edge : block.getAllSuccessors()) {
        DomBlock succ = edge.getDestination();

        if (edge.getType().isUnknown() && blocks.contains(succ)) {
          mapIncomingEdges.computeIfAbsent(succ, k -> new ArrayList<>()).add(edge);
          mapOutgoingEdges.computeIfAbsent(block, k -> new ArrayList<>()).add(edge);
        }
      }
    }

    Map<DomBlock, Integer> mapIncomingEdgesCount = new HashMap<>();
    for (DomBlock block : blocks) {
      mapIncomingEdgesCount.put(block, mapIncomingEdges.getOrDefault(block, List.of()).size());
    }

    // Step 1b: validate the incoming edges map
    //   - startBlock should have 0 incoming edges
    //   - all other blocks should have at least 1 incoming edge
    for (DomBlock block : blocks) {
      if (block == startBlock) {
        if (mapIncomingEdgesCount.getOrDefault(block, 0) != 0) {
          tracer.error(
            blockList,
            "Start block has " + mapIncomingEdges.get(block) + " incoming edges!",
            List.of(block)
          );
          throw new RuntimeException("Start block has " + mapIncomingEdgesCount.getOrDefault(block, 0) + " incoming edges!");
        }
      } else {
        if (mapIncomingEdgesCount.getOrDefault(block, 0) < 1) {
          tracer.error(
            blockList,
            "Block " + block + " has " + mapIncomingEdgesCount.getOrDefault(block, 0) + " incoming edges!",
            List.of(block)
          );
          throw new RuntimeException("Block " + block + " has " + mapIncomingEdgesCount.getOrDefault(block, 0) + " incoming edges!");
        }
      }
    }

    Set<DomBlock> unseenBlocks = new HashSet<>(blockList);
    unseenBlocks.remove(startBlock);
    DomBlock runningBlock = solveDAGPart(blockList, startBlock, tracer, mapIncomingEdgesCount, mapIncomingEdges, mapOutgoingEdges, unseenBlocks);

    if (!mapIncomingEdgesCount.isEmpty()) {
      tracer.error(blockList, "Not all blocks have been processed!", mapIncomingEdgesCount.keySet());
      throw new RuntimeException("Not all blocks have been processed!");
    }

    // Update escaping edges
    for (DomBlock block : blockList) {
      for (var edge : new ArrayList<>(block.getEnclosed())) {
        if (edge.getType().isUnknown()) {
          edge.changeClosure(runningBlock);
        }
      }
    }

    tracer.successCreated(blockList, "dagSolved", runningBlock);
    return runningBlock;
  }

  private static DomBlock solveDAGPart(
    List<DomBlock> blockList,
    DomBlock startBlock,
    DomTracer2 tracer,
    Map<DomBlock, Integer> mapIncomingEdgesCount,
    HashMap<DomBlock, List<DomEdge>> mapIncomingEdges,
    HashMap<DomBlock, List<DomEdge>> mapOutgoingEdges,
    Set<DomBlock> unseenBlocks
  ) {
    DomBlock runningBlock = null;

    List<DomBlock> nextBlockStack = new ArrayList<>();
    nextBlockStack.add(startBlock);

    while (!nextBlockStack.isEmpty()) {
      DomBlock block = nextBlockStack.remove(nextBlockStack.size() - 1);
      mapIncomingEdgesCount.remove(block);

      if (tracer.isDotOn()) {
        Map<DomBlock, String> props = new HashMap<>();
        if (runningBlock != null) {
          props.put(runningBlock, "fillcolor=orange,style=filled,xlabel=\"runningBlock\"");
        }
        int i = 0;
        for (DomBlock others : nextBlockStack) {
          props.put(others, "fillcolor=pink,style=filled,xlabel=\"nextBlockStack[" + (i - nextBlockStack.size()) + "]\"");
          i++;
        }
        props.put(block, "fillcolor=lightblue,style=filled,xlabel=\"block\"");

        List<DomBlock> lst = new ArrayList<>(props.keySet());
        lst.addAll(blockList);
        tracer.add(lst, "part-nextBlockStack", null, props);
      }

      List<DomEdge> outgoingEdges = mapOutgoingEdges.getOrDefault(block, List.of());

      if (block instanceof RawDomBlock rawBlock) {
        // resolve edges
        Map<DomEdge, DomBlock> mapEdges = new HashMap<>();
        for (DomEdge inlineEdge : rawBlock.inlinableEdges()) {
          DomBlock target = inlineEdge.getDestination();
          if (!unseenBlocks.contains(target) || mapIncomingEdges.get(target).size() > 1) {
            continue;
          }
          unseenBlocks.remove(target);
          outgoingEdges.remove(inlineEdge);

          DomBlock simplified = solveDAGPart(blockList, target, tracer, mapIncomingEdgesCount, mapIncomingEdges, mapOutgoingEdges, unseenBlocks);
          mapEdges.put(inlineEdge, simplified);
          List<DomBlock> lst = new ArrayList<>();
          simplified.getRecursive(lst);
        }

        block = rawBlock.resolve(mapEdges);
        mapIncomingEdges.put(block, mapIncomingEdges.getOrDefault(rawBlock, List.of()));

        List<DomBlock> lst = new ArrayList<>();
        lst.add(block);
        tracer.add(lst, "raw-resolved", null, new HashMap<>());


        if (tracer.isDotOn()) {
          Map<DomBlock, String> props = new HashMap<>();
          if (runningBlock != null) {
            props.put(runningBlock, "fillcolor=orange,style=filled,xlabel=\"runningBlock\"");
          }
          int i = 0;
          for (DomBlock others : nextBlockStack) {
            props.put(others, "fillcolor=pink,style=filled,xlabel=\"nextBlockStack[" + (i - nextBlockStack.size()) + "]\"");
            i++;
          }
          props.put(block, "fillcolor=lightblue,style=filled,xlabel=\"block\"");

          List<DomBlock> lst2 = new ArrayList<>(props.keySet());
          lst2.addAll(blockList);
          tracer.add(lst2, "part-nextBlockStack-resolved", null, props);
        }
      }

      List<DomBlock> currentSequence = new ArrayList<>();
      if (runningBlock == null) {
        // First block
        currentSequence.add(block);
      } else {
        if (!(runningBlock instanceof DomBasicBlock) && !(runningBlock instanceof DomSequenceBlock)) {
          runningBlock = new DomSequenceBlock(List.of(runningBlock));
        }

        currentSequence.add(runningBlock);
        // Mark all back edges as break edges from the runningBlock
        for (DomEdge backEdge : mapIncomingEdges.getOrDefault(block, List.of())) {
          backEdge.changeType(DomEdgeType.BREAK).changeClosure(runningBlock);
        }
        currentSequence.add(block);
      }

      while (outgoingEdges.size() == 1 && block.getAllSuccessors().size() == 1) {
        // We might be able to add the next block to the current sequence
        DomEdge outEdge = outgoingEdges.get(0);
        DomBlock nextBlock = outEdge.getDestination();
        List<DomEdge> incomingNextEdges = mapIncomingEdges.getOrDefault(nextBlock, List.of());
        if (incomingNextEdges.size() != 1 || nextBlock instanceof RawDomBlock) {
          break;
        }


        if (tracer.isDotOn()) {
          Map<DomBlock, String> props = new HashMap<>();
          int i = 0;
          for (DomBlock others : nextBlockStack) {
            props.put(others, "fillcolor=pink,style=filled,xlabel=\"nextBlockStack[" + (i - nextBlockStack.size()) + "]\"");
            i++;
          }
          for (DomBlock preSeq : currentSequence) {
            props.put(preSeq, "fillcolor=lightblue,style=filled,xlabel=\"currentSequence\"");
          }
          if (runningBlock != null) { // doing this later to override the current sequence one
            props.put(runningBlock, "fillcolor=orange,style=filled,xlabel=\"runningBlock\"");
          }
          props.put(nextBlock, "fillcolor=lightblue,style=filled,xlabel=\"block\"");

          List<DomBlock> lst = new ArrayList<>(props.keySet());
          lst.addAll(blockList);
          tracer.add(lst, "part-nextBlockStackExtended", null, props);
        }

        // Add the next block and mark the edge as regular
        currentSequence.add(nextBlock);
        if (outEdge.getSource() == block) {
          outEdge.changeType(DomEdgeType.REGULAR);
        } else {
          outEdge.changeType(DomEdgeType.BREAK).changeClosure(block);
        }
        mapIncomingEdgesCount.remove(nextBlock);

        outgoingEdges = mapOutgoingEdges.getOrDefault(nextBlock, List.of());
        block = nextBlock;
      }

      if (currentSequence.size() > 1) {
        runningBlock = new DomSequenceBlock(currentSequence);
      } else {
        // for the first block, if it wasn't combined with another block
        runningBlock = currentSequence.get(0);
      }

      // Check which other blocks are now ready to be added to the stack
      for (DomEdge outEdge : outgoingEdges) {
        DomBlock nextBlock = outEdge.getDestination();
        int currentCount = mapIncomingEdgesCount.getOrDefault(nextBlock, 0);
        if (currentCount <= 0) {
          throw new IllegalStateException("Block " + nextBlock + " has a negative incoming edge count!");
        } else if (currentCount == 1) {
          nextBlockStack.add(nextBlock);
          mapIncomingEdgesCount.remove(nextBlock);
        } else {
          mapIncomingEdgesCount.put(nextBlock, currentCount - 1);
        }
      }
    }
    return runningBlock;
  }

  // Returns a postdominator tree for a given general statement
  public static VBStyleCollection<List<Integer>, Integer> calcPostDominators(Statement general) {

    HashMap<Statement, FastFixedSet<Statement>> lists = new HashMap<>();

    // Calculate strong connectivity
    StrongConnectivityHelper schelper = new StrongConnectivityHelper(general);
    List<List<Statement>> components = schelper.getComponents();

    List<Statement> lstStats = general.getPostReversePostOrderList(StrongConnectivityHelper.getExitReps(components));

    FastFixedSetFactory<Statement> factory = FastFixedSetFactory.create(lstStats);

    FastFixedSet<Statement> setFlagNodes = factory.createCopiedSet();
    FastFixedSet<Statement> initSet = factory.createCopiedSet();

    for (List<Statement> component : components) {
      FastFixedSet<Statement> tmpSet;

      if (StrongConnectivityHelper.isExitComponent(component)) {
        tmpSet = factory.createEmptySet();
        tmpSet.addAll(component);
      } else {
        tmpSet = initSet.getCopy();
      }

      for (Statement stat : component) {
        lists.put(stat, tmpSet);
      }
    }

    do {
      for (Statement stat : lstStats) {

        if (!setFlagNodes.contains(stat)) {
          continue;
        }

        setFlagNodes.remove(stat);

        FastFixedSet<Statement> doms = lists.get(stat);
        FastFixedSet<Statement> domsSuccs = factory.createEmptySet();

        List<Statement> successors = stat.getNeighbours(StatEdge.TYPE_REGULAR, EdgeDirection.FORWARD);

        for (int j = 0; j < successors.size(); j++) {
          Statement succ = successors.get(j);
          FastFixedSet<Statement> succlst = lists.get(succ);

          // first
          if (j == 0) {
            // Union the sets as it is empty at this point
            domsSuccs.union(succlst);
          } else {
            domsSuccs.intersection(succlst);
          }
        }

        if (!domsSuccs.contains(stat)) {
          domsSuccs.add(stat);
        }

        if (!InterpreterUtil.equalObjects(domsSuccs, doms)) {

          lists.put(stat, domsSuccs);

          List<Statement> lstPreds = stat.getNeighbours(StatEdge.TYPE_REGULAR, EdgeDirection.BACKWARD);
          for (Statement pred : lstPreds) {
            setFlagNodes.add(pred);
          }
        }
      }
    } while (!setFlagNodes.isEmpty());

    VBStyleCollection<List<Integer>, Integer> postDominators = new VBStyleCollection<>();

    List<Statement> lstRevPost = general.getReversePostOrderList(); // sort order crucial!

    HashMap<Integer, Integer> mapSortOrder = new HashMap<>();
    for (int i = 0; i < lstRevPost.size(); i++) {
      mapSortOrder.put(lstRevPost.get(i).id, i);
    }

    for (Statement st : lstStats) {

      List<Integer> lstPosts = new ArrayList<>();

      for (Statement stt : lists.get(st)) {
        lstPosts.add(stt.id);
      }

      // The postdom list for this statement must be sorted based on the post reverse postorder of the general statement that it's contained in
      // This should lead to proper iteration during general statement creation.
      lstPosts.sort(Comparator.comparing(mapSortOrder::get));

      // After sorting, ensure that the statement that owns this postdominance list comes last, if it comes first.
      if (lstPosts.size() > 1 && lstPosts.get(0) == st.id) {
        lstPosts.add(lstPosts.remove(0));
      }

      postDominators.addWithKey(lstPosts, st.id);
    }

    return postDominators;
  }

  public static RootStatement parseGraph(ControlFlowGraph graph, StructMethod mt, int iteration) {

    DomTracer2 tracer = new DomTracer2("domhelper_" + iteration, mt);

    var conversionResult = initialConversion(graph, mt);
    conversionResult = processExceptions(conversionResult, graph, tracer);

    DomBlock single = processStatement(conversionResult.blocks, conversionResult.startBlock, tracer);
    if (single == null) {
      DotExporter.errorToDotFile(graph, mt, "parseGraphFail");
//      DotExporter.errorToDotFile(root, mt, "parseGraphFailStat");
      throw new RuntimeException("parsing failure!");
    }

    RootStatement root = convertToRootStatement(single, mt);
    root.addComments(graph);

    MethodProcessor.debugCurrentlyDecompiling.set(root);

    DotExporter.toDotFile(root, mt, "domhelper_" + iteration);

    ValidationHelper.validateStatement(root);

//    if (true) {
//    throw new RuntimeException("YOLO");
//    }

//
    LabelHelper.lowContinueLabels(root, new LinkedHashSet<>());
//
    SequenceHelper.condenseSequences(root);
    root.buildMonitorFlags();
//
//    // build synchronized statements
    buildSynchronized(root);
//
    return root;
  }

  private static InitialConversionResult processExceptions(
    InitialConversionResult conversionResult,
    ControlFlowGraph graph,
    DomTracer2 tracer
  ) {
    if (graph.getExceptions().isEmpty()) {
      return conversionResult;
    }
    // TODO: currently assumes that the exceptions are not obfuscated.
    DomBlock startBlock = conversionResult.startBlock;

    Map<BasicBlock, RawDomTryCatchBlock> tryCatchBlocks = new HashMap<>();

    for (var cfgRange : graph.getExceptions()) {
      Set<DomBlock> set = new LinkedHashSet<>();
      for (BasicBlock block : cfgRange.getProtectedRange()) {
        if (tryCatchBlocks.containsKey(block)) {
          set.add(tryCatchBlocks.get(block));
        } else {
          set.add(conversionResult.simpleToDom.get(block));
        }
      }

      DomBlock handler = conversionResult.simpleToDom.get(cfgRange.getHandler());

      // check if we can extend an existing try catch block
      if (set.size() == 1 && set.iterator().next() instanceof RawDomTryCatchBlock catchBlock) {
        if (catchBlock.protectedRange.size() != cfgRange.getProtectedRange().size()) {
          // TODO: Depending on inheritance, this may be a no-op or could be just normal as if the order were reversed
          throw new RuntimeException("A later try catch only covers a small sub-range of a previous range");
        }

        if (catchBlock.handlers.containsKey(cfgRange.getExceptionTypes())) {
          // TODO: technically this would always be a no-op, just raising an error rn cause handlers should be a list
          //       of pairs instead of a map, or we have to handle potentially dead code
          throw new RuntimeException("An earlier try catch already has a handler for this exception type");
        }

        var dummy = new RawDomCatchBlock(cfgRange.getExceptionTypes());
        DomEdge.create(catchBlock.tryBlock, dummy, null, DomEdgeType.EXCEPTION);

        catchBlock.handlers.put(
          cfgRange.getExceptionTypes(),
          DomEdge.create(dummy, handler, catchBlock, DomEdgeType.UNKNOWN)
        );
        catchBlock.handlerBlocks.put(
          cfgRange.getExceptionTypes(),
          dummy
        );
      }

      // TODO: check if for any try catch block, the protected range is not a complete subset of the current range

      DomBlock entryPoint = findEntryPoint(set, startBlock);
      var tryBlock = processStatement(new ArrayList<>(set), entryPoint, tracer);
      RawDomTryCatchBlock tryCatchBlock = new RawDomTryCatchBlock(cfgRange.getProtectedRange(), tryBlock);

      for (var edge : new ArrayList<>(entryPoint.getPredecessors())) {
        if (edge.getType().isUnknown()) {
          edge.changeDestination(tryCatchBlock);
        }
      }

      // Pull up all enclosed edges
      for (var edge : tryBlock.getAllSuccessors()) {
        if (edge.getType().isUnknown()) {
          edge.changeClosure(tryCatchBlock);
        }
      }

      // Add handler edge
      var dummy = new RawDomCatchBlock(cfgRange.getExceptionTypes());
      DomEdge.create(tryBlock, dummy, null, DomEdgeType.EXCEPTION);
      tryCatchBlock.handlers.put(
        cfgRange.getExceptionTypes(),
        DomEdge.create(dummy, handler, tryCatchBlock, DomEdgeType.UNKNOWN)
      );
      tryCatchBlock.handlerBlocks.put(
        cfgRange.getExceptionTypes(),
        dummy
      );

      // Remap the blocks
      for (BasicBlock block : cfgRange.getProtectedRange()) {
        DomBlock oldBlock = tryCatchBlocks.put(block, tryCatchBlock);
        if (oldBlock == null) {
          oldBlock = conversionResult.simpleToDom.get(block);
        }
        if (oldBlock == startBlock) {
          startBlock = tryCatchBlock;
        }
      }
    }

    // Fix the conversion result
    conversionResult.simpleToDom.keySet().removeAll(tryCatchBlocks.keySet());
    List<DomBlock> lstDomBlocks = new ArrayList<>(conversionResult.simpleToDom.values());
    lstDomBlocks.addAll(new LinkedHashSet<>(tryCatchBlocks.values()));
    return new InitialConversionResult(
      lstDomBlocks,
      startBlock,
      null
    );
  }

  private static DomBlock findEntryPoint(Set<DomBlock> set, DomBlock extraEntryPoint) {
    DomBlock entryPoint = null;

    for (DomBlock block : set) {
      for (var edge : block.getPredecessors()) {
        DomBlock source = edge.getSource();
        if (edge.getClosure() != null) {
          source = edge.getClosure();
        }

        if (set.contains(source)) {
          continue;
        }

        if (entryPoint == null) {
          entryPoint = block;
          break;
        } else if (entryPoint == block) {
          break;
        } else {
          throw new RuntimeException("Entry point not unique!");
        }
      }
    }

    if (extraEntryPoint != null && set.contains(extraEntryPoint)) {
      if (entryPoint == null) {
        entryPoint = extraEntryPoint;
      } else if (entryPoint != extraEntryPoint) {
        throw new RuntimeException("Entry point not unique!");
      }
    }

    if (entryPoint == null) {
      throw new RuntimeException("No entry point found!");
    }

    return entryPoint;
  }

  private static RootStatement convertToRootStatement(DomBlock block, StructMethod mt) {
    // Convert all basic blocks first
    List<DomBlock> doms = new ArrayList<>();
    block.getRecursive(doms);

    Map<DomBlock, Statement> blockToStatement = new HashMap<>();

    for (DomBlock domBlock : doms) {
      if (domBlock instanceof DomBasicBlock basicBlock && basicBlock.block != null) {
        blockToStatement.put(domBlock, new BasicBlockStatement(basicBlock.block));
      }
    }

    for (DomBlock domBlock : doms) {
      if (domBlock instanceof DomBasicBlock basicBlock && basicBlock.block == null) {
        BasicBlock fakeBlock = new BasicBlock(blockToStatement.size() + 1);
        blockToStatement.put(domBlock, new BasicBlockStatement(fakeBlock));
      }
    }

    DummyExitStatement dummyExit = new DummyExitStatement();

    Statement topLevel = convertToStatement(block, blockToStatement);
    addEdges(block, blockToStatement, dummyExit);

    topLevel.setAllParent(true);

    RootStatement root = new RootStatement(topLevel, dummyExit, mt);
    root.setAllParent();

    for (var edge : dummyExit.getAllPredecessorEdges()) {
      edge.changeClosure(root);
    }

    return root;
  }

  private static StatEdge convertEdge(
    DomEdge edge,
    Map<DomBlock, Statement> blockToStatement,
    DummyExitStatement dummyExit
  ) {
    Statement source = blockToStatement.get(edge.getSource());
    if (edge.getType() == DomEdgeType.EXIT) {
      StatEdge statEdge = new StatEdge(StatEdge.TYPE_BREAK, source, dummyExit);
      source.addSuccessor(statEdge);
      return statEdge;
    }

    Statement destination = blockToStatement.get(edge.getDestination());
    Statement closure = edge.getClosure() == null ? null : blockToStatement.get(edge.getClosure());

    int type = switch (edge.getType()) {
      case UNKNOWN -> throw new RuntimeException("Did not expect unknown edge type");
      case UNKNOWN_EXCEPTION -> throw new RuntimeException("Did not expect unknown exception edge type");
      case REGULAR -> StatEdge.TYPE_REGULAR;
      case CONTINUE -> StatEdge.TYPE_CONTINUE;
      case BREAK -> StatEdge.TYPE_BREAK;
      case EXCEPTION -> StatEdge.TYPE_EXCEPTION;
      case EXIT -> throw new IllegalStateException("Should not be here");
    };

    StatEdge statEdge = new StatEdge(type, source, destination, closure);
    source.addSuccessor(statEdge);
    return statEdge;
  }

  private static void addEdges(
    DomBlock block,
    Map<DomBlock, Statement> blockToStatement,
    DummyExitStatement dummyExit) {
    for (DomEdge edge : block.getSuccessors()) {
      convertEdge(edge, blockToStatement, dummyExit);
    }

    for (DomBlock child : block.getChildren()) {
      addEdges(child, blockToStatement, dummyExit);
    }

    if (block instanceof DomIfBlock ifBlock) {
      IfStatement ifStatement = (IfStatement) blockToStatement.get(ifBlock);
      Statement head = ifStatement.getFirst();
      List<StatEdge> successors = head.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL);
      if (successors.size() != 2) {
        throw new RuntimeException("If head has " + successors.size() + " successors!");
      }
      ifStatement.setIfEdge(successors.get(0));
      StatEdge elseEdge = successors.get(1);
      elseEdge.changeSource(ifStatement);
      if (elseEdge.getType() == StatEdge.TYPE_BREAK && elseEdge.closure == ifStatement) {
        elseEdge.changeClosure(null);
        elseEdge.changeType(StatEdge.TYPE_REGULAR);
      }
      ifStatement.setNegated(true);
    } else if (block instanceof DomSwitchBlock switchBlock) {
      SwitchStatement switchStat = (SwitchStatement) blockToStatement.get(block);
      List<StatEdge> edges = switchStat.getFirst().getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL);
      if (edges.size() != 1 + switchBlock.getValues().length) {
        throw new RuntimeException("Switch statement has " + edges.size() + " edges!");
      }
      switchStat.setDefaultEdge(edges.get(0));

      var caseEdges = switchStat.getCaseEdges();
      var caseValues = switchStat.getCaseValues();
      var caseStatements = switchStat.getCaseStatements();
      for (int i = 0; i < switchBlock.getValues().length; i++) {
        StatEdge edge = edges.get(i + 1);
        var wrapped = new ArrayList<StatEdge>();
        wrapped.add(edge);
        caseEdges.add(wrapped);
        var caseValue = switchBlock.getValues()[i];

        var caseWrapper = new ArrayList<Exprent>();
        caseWrapper.add(new ConstExprent(caseValue, false, null));
        caseValues.add(caseWrapper);
        caseStatements.add(null);
      }
      var wrapped = new ArrayList<StatEdge>();
      wrapped.add(edges.get(0));
      caseEdges.add(wrapped);
      var caseWrapped = new ArrayList<Exprent>();
      caseWrapped.add(null);
      caseValues.add(caseWrapped);
      caseStatements.add(null);
    } else if (block instanceof DomLoopBlock || block instanceof DomSequenceBlock) {
      Statement stat = blockToStatement.get(block);
      for (var edge : stat.getLabelEdges()) {
        if (edge.getType() == StatEdge.TYPE_BREAK) {
          stat.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, stat, edge.getDestination()));
          break; // only add regular out edge
        }
      }
    } else if (block instanceof DomTryCatchBlock) {
      Statement stat = blockToStatement.get(block);
      if (stat instanceof CatchAllStatement catchAll) {
        for (var edge : catchAll.getLabelEdges()) {
          if (edge.getType() == StatEdge.TYPE_BREAK) {
            catchAll.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, catchAll, edge.getDestination()));
            break; // only add regular out edge
          }
        }
      } else if (stat instanceof CatchStatement tryCatch) {
        for (var edge : tryCatch.getLabelEdges()) {
          if (edge.getType() == StatEdge.TYPE_BREAK) {
            tryCatch.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, tryCatch, edge.getDestination()));
            break; // only add regular out edge
          }
        }
      } else {
        throw new RuntimeException("Unexpected statement type");
      }
    }
  }

  private static Statement convertToStatement(
    DomBlock block,
    Map<DomBlock, Statement> blockToStatement
  ) {
    if (block instanceof DomBasicBlock) {
      return blockToStatement.get(block);
    } else if (block instanceof DomIfBlock ifBlock) {
      Statement head = convertToStatement(ifBlock.head, blockToStatement);
      IfStatement ifStat = new IfStatement(head);
      blockToStatement.put(block, ifStat);
      return ifStat;
    } else if (block instanceof DomSwitchBlock switchBlock) {
      Statement head = convertToStatement(switchBlock.head, blockToStatement);
      Statement defaultBlock = convertToStatement(switchBlock.defaultBlock, blockToStatement);
      SwitchStatement switchStat = new SwitchStatement(head);
      for (DomBlock subBlock : switchBlock.blocks) {
        Statement child = convertToStatement(subBlock, blockToStatement);
        switchStat.getCaseStatements().add(child);
        switchStat.getStats().addWithKey(child, child.id);
      }
      switchStat.getCaseStatements().add(defaultBlock);
      switchStat.getStats().addWithKey(defaultBlock, defaultBlock.id);
      blockToStatement.put(block, switchStat);
      return switchStat;
    } else if (block instanceof DomLoopBlock loopBlock) {
      Statement body = convertToStatement(loopBlock.body, blockToStatement);
      Statement loop = new DoStatement(body);
      blockToStatement.put(block, loop);
      return loop;
    } else if (block instanceof DomSequenceBlock sequenceBlock) {
      List<Statement> lst = new ArrayList<>();
      for (DomBlock subBlock : sequenceBlock.blocks) {
        Statement sub = convertToStatement(subBlock, blockToStatement);
        lst.add(sub);
      }
      Statement seq = new SequenceStatement(lst);
      blockToStatement.put(block, seq);
      return seq;
    } else if (block instanceof DomTryCatchBlock tryCatchBlock) {
      Statement head = convertToStatement(tryCatchBlock.tryBlock, blockToStatement);
      Map<List<String>, Statement> handlers = new LinkedHashMap<>();
      if (tryCatchBlock.handlerBlocks.size() == 1) {
        var h = tryCatchBlock.handlerBlocks.keySet().iterator().next();
        if (h.size() == 1 && h.get(0).equals("Ljava/lang/Throwable")) {
          // FINALLY (TEMP)
          Statement handler = convertToStatement(tryCatchBlock.handlerBlocks.get(h), blockToStatement);
          CatchAllStatement tryCatch = new CatchAllStatement(head, handler);
          blockToStatement.put(block, tryCatch);
          return tryCatch;
        }
      }
      for (var entry : tryCatchBlock.handlerBlocks.entrySet()) {
        var handler = convertToStatement(entry.getValue(), blockToStatement);
        handlers.put(entry.getKey(), handler);
      }
      CatchStatement tryCatch = new CatchStatement(head, handlers);
      blockToStatement.put(block, tryCatch);
      return tryCatch;
    } else {
      throw new RuntimeException("Unknown block type: " + block.getClass());
    }
  }

  public static boolean removeSynchronizedHandler(Statement stat) {
    boolean res = false;

    for (Statement st : stat.getStats()) {
      res |= removeSynchronizedHandler(st);
    }

    if (stat instanceof SynchronizedStatement) {
      ((SynchronizedStatement) stat).removeExc();
      res = true;
    }

    return res;
  }


  private static void buildSynchronized(Statement stat) {

    for (Statement st : stat.getStats()) {
      buildSynchronized(st);
    }

    if (stat instanceof SequenceStatement) {

      while (true) {

        boolean found = false;

        List<Statement> lst = stat.getStats();
        for (int i = 0; i < lst.size() - 1; i++) {
          Statement current = lst.get(i);  // basic block

          if (current.isMonitorEnter()) {

            Statement next = lst.get(i + 1);
            Statement nextDirect = next;

            while (next instanceof SequenceStatement) {
              next = next.getFirst();
            }

            if (next instanceof CatchAllStatement) {

              CatchAllStatement ca = (CatchAllStatement) next;

              boolean headOk = ca.getFirst().containsMonitorExitOrAthrow();

              if (!headOk) {
                headOk = hasNoExits(ca.getFirst());
              }

              // If the body of the monitor ends in a throw, it won't have a monitor exit as the catch handler will call it.
              // We will also not have a monitorexit in an infinite loop as there is no way to leave the statement.
              // However, the handler *must* have a monitorexit!
              if (headOk && ca.getHandler().containsMonitorExit()) {

                // remove monitorexit
                ca.getFirst().markMonitorexitDead();
                ca.getHandler().markMonitorexitDead();

                // remove the head block from sequence
                current.removeSuccessor(current.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL).get(0));

                for (StatEdge edge : current.getPredecessorEdges(Statement.STATEDGE_DIRECT_ALL)) {
                  current.removePredecessor(edge);
                  edge.getSource().changeEdgeNode(EdgeDirection.FORWARD, edge, nextDirect);
                  nextDirect.addPredecessor(edge);
                }

                stat.getStats().removeWithKey(current.id);
                stat.setFirst(stat.getStats().get(0));

                // new statement
                SynchronizedStatement sync = new SynchronizedStatement(current, ca.getFirst(), ca.getHandler());
                sync.setAllParent();

                for (StatEdge edge : new HashSet<>(ca.getLabelEdges())) {
                  sync.addLabeledEdge(edge);
                }

                current.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, current, ca.getFirst()));

                ca.getParent().replaceStatement(ca, sync);
                found = true;
                break;
              }
            }
          }
        }

        if (!found) {
          break;
        }
      }
    }
  }

  // Checks if a statement has no exits (disregarding exceptions) that lead outside the statement.
  private static boolean hasNoExits(Statement head) {
    Deque<Statement> stack = new ArrayDeque<>();
    stack.add(head);

    while (!stack.isEmpty()) {
      Statement stat = stack.removeFirst();

      List<StatEdge> sucs = stat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL);
      for (StatEdge suc : sucs) {
        if (!head.containsStatement(suc.getDestination())) {
          return false;
        }
      }

      stack.addAll(stat.getStats());
    }

    return true;
  }

  private static DomBlock processStatement(
    List<DomBlock> doms,
    DomBlock ourEntryPoint,
    DomTracer2 tracer
  ) {
    tracer.info(doms, "process statement", Map.of(ourEntryPoint, "fillcolor=orange,style=filled,xlabel=\"entryPoint\""));

    // STEP 1: split up a general statement into connected components
    //   each connected component is one of the following:
    //    * a bunch of statements that are part of the same loop
    //    * a single statement that loops back to itself
    //    * a single statement

    var components = StrongConnectivityHelper2.analyse(doms, ourEntryPoint);
    List<DomBlock> simpleStatements = new ArrayList<>(components.components().size());

    boolean success = true;

    for (int i = 0; i < components.components().size(); i++) {
      List<DomBlock> lst = components.components().get(i);
      if (lst.size() == 1) {
        // check if the statement doesn't loop back to itself
        DomBlock st = lst.get(0);


        if (st.getAllSuccessors().stream().noneMatch(edge ->
          edge.getDestination() == st && edge.getType().isUnknown())) {
          simpleStatements.add(st);
          continue;
        }
      }

      // Extract the loop into a new general statement
      Set<DomBlock> entryPoints = components.entryPoints().get(i);
      if (entryPoints.size() != 1) {
        Set<DomBlock> all = new HashSet<>(doms);
        all.addAll(simpleStatements);
        Map<DomBlock, String> props = new HashMap<>();
        for (DomBlock domBlock : lst) {
          if (entryPoints.contains(domBlock)) {
            props.put(domBlock, "fillcolor=orange,style=filled,xlabel=\"entryPoint\"");
          } else {
            props.put(domBlock, "fillcolor=pink,style=filled,xlabel=\"component part\"");
          }
        }
        tracer.error(all, "Components should have only one entry point", props);
        throw new RuntimeException("Components should have only one entry point");
      }
      var entryPoint = entryPoints.iterator().next();

      // All unknown out edges from a statement in the loop to the entry point should become continue edges
      var scopedEdges = new HashSet<DomEdge>();
      for (var st : lst) {
        for (var edge : st.getAllSuccessors()) {
          if (edge.getType() != DomEdgeType.UNKNOWN) {
            continue;
          }

          DomBlock dest = edge.getDestination();
          if (entryPoint == dest) {
            edge.changeType(DomEdgeType.CONTINUE);
            scopedEdges.add(edge);
          }
        }
      }

      // Recursively transform newStat
      if (lst.size() > doms.size()) {
        // something went wrong
        throw new RuntimeException("Anti stack overflow");
      }
      DomBlock simplified = processStatement(lst, entryPoint, tracer);
      if (simplified == null) {
        Set<DomBlock> all = new HashSet<>(doms);
        all.addAll(simpleStatements);
        tracer.error(all, "General statement processing failed!", lst);
//        simpleStatements.add(newStat);
        success = false;
        continue;
      }

      DomLoopBlock loop = new DomLoopBlock(simplified);

      // Mark all continue as having this loop as their context
      for (var edge : scopedEdges) {
        edge.changeClosure(loop);
        edge.changeDestination(loop);
      }

      for (var edge : new ArrayList<>(simplified.getEnclosed())) {
        if (edge.getType().isUnknown()) {
          edge.changeClosure(loop);
        }
      }

      simpleStatements.add(loop);

      if (entryPoint == ourEntryPoint) {
        ourEntryPoint = loop;
      }

      // Remap all edges to the entrypoint to the new statement
      for (var edge : new ArrayList<>(entryPoint.getPredecessors())) {
        if (edge.getType() == DomEdgeType.CONTINUE) {
          continue;
        }
        edge.changeDestination(loop);
      }
    }

    tracer.info(simpleStatements, "post-process statement");

    return solveDAG(simpleStatements, ourEntryPoint, tracer);
  }

  private static boolean checkSynchronizedCompleteness(Set<Statement> setNodes) {
    // check exit nodes
    for (Statement stat : setNodes) {
      if (stat.isMonitorEnter()) {
        List<StatEdge> lstSuccs = stat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL);
        if (lstSuccs.size() != 1 || lstSuccs.get(0).getType() != StatEdge.TYPE_REGULAR) {
          return false;
        }

        if (!setNodes.contains(lstSuccs.get(0).getDestination())) {
          return false;
        }
      }
    }

    return true;
  }
}
