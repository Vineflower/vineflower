// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.java.decompiler.code.*;
import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.code.cfg.ControlFlowGraph;
import org.jetbrains.java.decompiler.code.cfg.ExceptionRangeCFG;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.code.DeadCodeHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectEdge;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectEdgeType;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.SSAConstructorSparseEx;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.SSAUConstructorSparseEx;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.SimpleSSAReassign;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchAllStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DummyExitStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsGraph;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.Pair;
import org.jetbrains.java.decompiler.util.collections.ListStack;

import java.util.*;
import java.util.Map.Entry;

@NotNullByDefault
public class FinallyProcessor {
  private final Map<BasicBlock, @Nullable Integer> finallyBlocks = new HashMap<>();
  // seems to store catch-alls that can't be converted to a finally
  private final Set<BasicBlock> catchallBlocks = new HashSet<>();

  private final MethodDescriptor methodDescriptor;
  private final VarProcessor varProcessor;
  private @Nullable VarVersionsGraph ssuversions;
  private @Nullable Map<Instruction, Integer> instrRewrites;

  public FinallyProcessor(StructMethod mt, MethodDescriptor md, VarProcessor varProc) {
    this.methodDescriptor = md;
    this.varProcessor = varProc;
  }

  public boolean iterateGraph(StructClass cl, StructMethod mt, RootStatement root, ControlFlowGraph graph) {
    this.ssuversions = null;
    BytecodeVersion bytecodeVersion = mt.getBytecodeVersion();

    ListStack<Statement> stack = new ListStack<>();
    stack.add(root);

    while (!stack.isEmpty()) {
      Statement stat = stack.pop();

      Statement parent = stat.getParent();
      if (parent instanceof CatchAllStatement fin && stat == parent.getFirst() && !parent.isCopied()) {
        BasicBlock head = fin.getBasichead().getBlock();
        BasicBlock handler = fin.getHandler().getBasichead().getBlock();

        //noinspection StatementWithEmptyBody
        if (this.catchallBlocks.contains(handler)) {
          // Already checked, can't be replaced by a finally statement.
          // do nothing
        } else if (this.finallyBlocks.containsKey(handler)) {
          // Already validated and transformed. This is a finally statement.
          fin.setFinally(true);

          Integer var = this.finallyBlocks.get(handler);
          fin.setMonitor(var == null ? null : new VarExprent(var, VarType.VARTYPE_INT, this.varProcessor));
        } else {
          // Check if this is a finally statement.
          Record inf = this.getFinallyInformation(cl, mt, root, fin);

          if (inf == null) { // inconsistent finally
            this.catchallBlocks.add(handler);
            root.addComment("$VF: Could not inline inconsistent finally blocks", true);
          } else {
            if (DecompilerContext.getOption(IFernflowerPreferences.FINALLY_DEINLINE) && this.verifyFinallyEx(graph, fin, inf)) {
              // Finally transformation was successful.
              inlineReturnVar(graph, handler, inf);

              this.finallyBlocks.put(handler, null);
            } else {
              // Finally merging failed.
              int varIndex = DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.VAR_COUNTER);
              // Add the semaphore variable to the list so we can create a comment in the output
              this.varProcessor.getSyntheticSemaphores().add(varIndex);
              insertSemaphore(graph, getAllBasicBlocks(fin.getFirst()), head, handler, varIndex, inf, bytecodeVersion);

              this.finallyBlocks.put(handler, varIndex);

              if (DecompilerContext.getOption(IFernflowerPreferences.DECOMPILER_COMMENTS)) {
                root.addComment("$VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.", true);
              }
            }

            DeadCodeHelper.removeDeadBlocks(graph); // e.g. multiple return blocks after a nested finally
            DeadCodeHelper.removeEmptyBlocks(graph);
            DeadCodeHelper.mergeBasicBlocks(graph);
          }

          return true;
        }
      }

      stack.addAll(stat.getStats());
    }

    return false;
  }

  private enum FinallyType {
    OTHER,  // Exception is kept in a stack var.
    DROP, // Drop the exception immediately after catching it, can't be rethrown
    STORE, // Store the exception immediately in a variable after catching it.
    EMPTY, //  The finally is empty?
  }

  private enum ExitType {
    IMPLICIT_EXIT,  // Normal end of the finally
    EXPLICIT_EXIT,  // `break` and `continue` that leave a finally block
    METHOD_EXIT,  // `return` or `throw` inside a finally block
  }

  private record Record(FinallyType finallyType, int exceptionOffset, Map<BasicBlock, ExitType> mapLast) {
  }

  private record Area(BasicBlock start, Set<BasicBlock> sample, @Nullable BasicBlock next /* true exit */,
                      Set<BasicBlock> sideExits) {
  }

  private record FinallyExit(BasicBlock source, BasicBlock succ, ExitType type) {
  }

  private @Nullable Record getFinallyInformation(StructClass cl, StructMethod mt, RootStatement root, CatchAllStatement fstat) {
    ExprProcessor proc = new ExprProcessor(this.methodDescriptor, this.varProcessor);
    proc.processStatement(root, cl);

    if (this.ssuversions == null) {
      // FIXME: don't split SSAU unless needed!
      SSAConstructorSparseEx ssa = new SSAConstructorSparseEx();
      ssa.splitVariables(root, mt);

      this.instrRewrites = SimpleSSAReassign.reassignSSAForm(ssa, root);

      StackVarsProcessor.setVersionsToNull(root);

      SSAUConstructorSparseEx ssau = new SSAUConstructorSparseEx();
      ssau.splitVariables(root, mt);

      this.ssuversions = ssau.getSsuVersions();
      StackVarsProcessor.setVersionsToNull(root);
    }

    Map<BasicBlock, ExitType> mapLast = new LinkedHashMap<>();

    BasicBlockStatement firstBlockStatement = fstat.getHandler().getBasichead();
    BasicBlock firstBasicBlock = firstBlockStatement.getBlock();

    if (firstBasicBlock.getSeq().isEmpty()) {
      return null;
    }

    Instruction instrFirst = firstBasicBlock.getInstruction(0);

    FinallyType firstcode = switch (instrFirst.opcode) {
      case CodeConstants.opc_pop -> FinallyType.DROP;
      case CodeConstants.opc_astore -> FinallyType.STORE;
      default -> FinallyType.OTHER;
    };

    SSAConstructorSparseEx ssa = new SSAConstructorSparseEx();
    ssa.splitVariables(root, mt);

    List<Exprent> lstExprents = firstBlockStatement.getExprents();
    ValidationHelper.notNull(lstExprents);  // All BBStatements will have exprents by now

    // A catch block always starts with an assignment from a fake catch var to stack index 0
    // In case of a STORE type, we don't care about this temp var, and immediately want to know where this stack var is
    //  saved instead.
    VarVersionPair varpaar = new VarVersionPair((VarExprent) ((AssignmentExprent) lstExprents.get(firstcode == FinallyType.STORE ? 1 : 0)).getLeft());

    FlattenStatementsHelper flatthelper = new FlattenStatementsHelper();
    DirectGraph dgraph = flatthelper.buildDirectGraph(root);

    LinkedList<DirectNode> stack = new LinkedList<>();
    stack.add(dgraph.first);

    Set<DirectNode> setVisited = new HashSet<>();
    Statement handler = fstat.getHandler();

    while (!stack.isEmpty()) {
      DirectNode node = stack.removeFirst();

      if (setVisited.contains(node) || node.statement instanceof DummyExitStatement) {
        // skip dummy.
        //  this is related to how it selects the blockStatement and could be
        //  removed when the selection is changed.
        continue;
      }
      setVisited.add(node);

      // Null means the catch var leaked in a way that can't happen with true "finally"s.
      //  This is thus not a true finally, return null.
      // Will return "explicit" even if the block does not leave the handler. It indicates that
      //  IF this block is an exit, it's an explicit type.
      @Nullable ExitType exitType = getExitType(firstcode, firstBlockStatement, node, varpaar);
      if (exitType == null) return null;

      for (DirectEdge suc : node.getSuccessors(DirectEdgeType.REGULAR)) {
        stack.add(suc.getDestination());
      }

      BasicBlockStatement blockStatement;
      if (node.block != null) {
        blockStatement = node.block;
      } else if (node.getPredecessors(DirectEdgeType.REGULAR).size() == 1) {
        DirectNode source = node.getPredecessors(DirectEdgeType.REGULAR).get(0).getSource();
        if (source.block == null) {
          continue;
        }
        blockStatement = source.block;
      } else {
        continue;
      }

      if (blockStatement.getBlock() == null || !handler.containsStatement(blockStatement)) {
        continue;
      }

      for (StatEdge edge : blockStatement.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL)) {
        if (edge.getType() != StatEdge.TYPE_REGULAR &&
          !handler.containsStatement(edge.getDestination())) {

          ExitType existingFlag = mapLast.get(blockStatement.getBlock());

          if (existingFlag != ExitType.IMPLICIT_EXIT) {
            mapLast.put(blockStatement.getBlock(), exitType);
            break;
          }
        }
      }
    }

    // empty finally block?
    if (fstat.getHandler() instanceof BasicBlockStatement) {

      boolean isFirstLast = mapLast.containsKey(firstBasicBlock);
      InstructionSequence seq = firstBasicBlock.getSeq();

      // Check if empty
      if (switch (firstcode) {
        case OTHER -> isFirstLast && seq.length() == 1;
        case DROP -> seq.length() == 1;
        case STORE -> isFirstLast ? seq.length() == 3 : seq.length() == 1;
        default -> false;
      }) {
        firstcode = FinallyType.EMPTY;
      }
    }

    return new Record(firstcode, instrFirst.startOffset, mapLast);
  }

  private static @Nullable ExitType getExitType(
    FinallyType finallyType,
    BasicBlockStatement firstBlockStatement,
    DirectNode node,
    VarVersionPair varPair
  ) {
    // Try to find the "true path" of the finally block by searching for a relevant 'athrow <var>'.
    // If we encounter a usage of <var> that isn't a throw, then, this isn't a valid finally.

    return switch (finallyType) {
      case DROP -> ExitType.IMPLICIT_EXIT;  // Why is this considered an implicit exit?
      case EMPTY -> throw new IllegalStateException("Empty detection has not been done yet?");
      case STORE -> {
        // Skip the `var10000 = varx;` and `vary = var10000` statements on the entry block
        int startIdx = firstBlockStatement == node.block ? 2 : 0;

        for (int i = startIdx; i < node.exprents.size(); i++) {
          Exprent exprent = node.exprents.get(i);

          // The exception is in a var, so we need to look for `<stack var> = <var>`, and then a `throw <stack var>`
          if (exprent instanceof AssignmentExprent assExpr &&
            assExpr.getRight() instanceof VarExprent varExprent &&
            varExprent.getVarVersionPair().equals(varPair)) {

            Exprent next = null;
            if (i != node.exprents.size() - 1) {
              next = node.exprents.get(i + 1);
            } else if (node.getSuccessors(DirectEdgeType.REGULAR).size() == 1) {
              // TODO: make a test case that uses this. I don't feel like later code considers this scenario correctly
              DirectNode nd = node.getSuccessors(DirectEdgeType.REGULAR).get(0).getDestination();
              if (!nd.exprents.isEmpty()) {
                next = nd.exprents.get(0);
              }
            }


            if (next instanceof ExitExprent exExpr &&
              exExpr.getExitType() == ExitExprent.Type.THROW &&
              exExpr.getValue() instanceof VarExprent &&
              assExpr.getLeft().equals(exExpr.getValue())) {
              // found normal exit
              yield ExitType.IMPLICIT_EXIT;
            } else {
              // found illegal usage of exception var.
              yield null;
            }
          }

          if (exprent instanceof ExitExprent) {
            // We found a method exit
            yield ExitType.METHOD_EXIT;
          }
        }
        yield ExitType.EXPLICIT_EXIT;
      }
      case OTHER -> {
        // Skip the `var10000 = varx` statement on the entry block
        int startIdx = firstBlockStatement == node.block ? 1 : 0;

        for (int i = startIdx; i < node.exprents.size(); i++) {
          Exprent exprent = node.exprents.get(i);

          if (exprent instanceof ExitExprent exExpr &&
            exExpr.getExitType() == ExitExprent.Type.THROW &&
            exExpr.getValue() instanceof VarExprent varExpr &&
            varExpr.getVarVersionPair().equals(varPair)) {
            // Found `throw <stack var>`
            yield ExitType.IMPLICIT_EXIT;
          }

          for (VarVersionPair exprVar : exprent.getAllVariables()) {
            if (exprVar.equals(varPair)) {
              // Illegal usage of <stack var> containing exception
              yield null;
            }
          }

          if (exprent instanceof ExitExprent) {
            // We found a method exit
            yield ExitType.METHOD_EXIT;
          }
        }
        yield ExitType.EXPLICIT_EXIT;
      }
    };
  }

  private static void insertSemaphore(
    ControlFlowGraph graph,
    Set<BasicBlock> setTry,
    BasicBlock head,
    BasicBlock handler,
    int var,
    Record information,
    BytecodeVersion bytecode_version) {
    Set<BasicBlock> setCopy = new HashSet<>(setTry);

    FinallyType finallytype = information.finallyType;
    Map<BasicBlock, ExitType> mapLast = information.mapLast();

    // first and last statements
    removeExceptionInstructionsEx(handler, 1, finallytype);
    for (Entry<BasicBlock, ExitType> entry : mapLast.entrySet()) {
      BasicBlock last = entry.getKey();

      if (entry.getValue() == ExitType.IMPLICIT_EXIT) {
        removeExceptionInstructionsEx(last, 2, finallytype);
        graph.getFinallyExits().add(last);
      }
    }

    final int store_length = var <= 3 ? 1 : var <= 128 ? 2 : 4;
    // disable semaphore at statement exit points
    for (BasicBlock block : setTry) {
      List<BasicBlock> lstSucc = block.getSuccs();

      for (BasicBlock dest : lstSucc) {
        // break out
        if (dest != graph.getLast() && !setCopy.contains(dest)) {
          // disable semaphore
          InstructionSequence seq = new InstructionSequence();
          seq.addInstruction(Instruction.create(CodeConstants.opc_bipush, false, CodeConstants.GROUP_GENERAL, bytecode_version, new int[]{0}, -1, 1));
          seq.addInstruction(Instruction.create(CodeConstants.opc_istore, false, CodeConstants.GROUP_GENERAL, bytecode_version, new int[]{var}, -1, store_length));

          // build a separate block
          BasicBlock newblock = new BasicBlock(++graph.last_id);
          newblock.setSeq(seq);

          // insert between block and dest
          block.replaceSuccessor(dest, newblock);
          newblock.addSuccessor(dest);
          setCopy.add(newblock);
          graph.getBlocks().addWithKey(newblock, newblock.id);

          // exception ranges
          // FIXME: special case synchronized

          // copy exception edges and extend protected ranges
          for (int j = 0; j < block.getSuccExceptions().size(); j++) {
            BasicBlock hd = block.getSuccExceptions().get(j);
            newblock.addSuccessorException(hd);

            ExceptionRangeCFG range = graph.getExceptionRange(hd, block);
            range.getProtectedRange().add(newblock);
          }
        }
      }
    }

    // enable semaphore at the statement entrance
    InstructionSequence seq = new InstructionSequence();
    seq.addInstruction(Instruction.create(CodeConstants.opc_bipush, false, CodeConstants.GROUP_GENERAL, bytecode_version, new int[]{1}, -1, 1));
    seq.addInstruction(Instruction.create(CodeConstants.opc_istore, false, CodeConstants.GROUP_GENERAL, bytecode_version, new int[]{var}, -1, store_length));

    BasicBlock newhead = new BasicBlock(++graph.last_id);
    newhead.setSeq(seq);

    insertBlockBefore(graph, head, newhead);

    // initialize semaphor with false
    seq = new InstructionSequence();
    seq.addInstruction(Instruction.create(CodeConstants.opc_bipush, false, CodeConstants.GROUP_GENERAL, bytecode_version, new int[]{0}, -1, 1));
    seq.addInstruction(Instruction.create(CodeConstants.opc_istore, false, CodeConstants.GROUP_GENERAL, bytecode_version, new int[]{var}, -1, store_length));

    BasicBlock newheadinit = new BasicBlock(++graph.last_id);
    newheadinit.setSeq(seq);

    insertBlockBefore(graph, newhead, newheadinit);

    setCopy.add(newhead);
    setCopy.add(newheadinit);

    for (BasicBlock hd : new HashSet<>(newheadinit.getSuccExceptions())) {
      ExceptionRangeCFG range = graph.getExceptionRange(hd, newheadinit);

      if (setCopy.containsAll(range.getProtectedRange())) {
        newheadinit.removeSuccessorException(hd);
        range.getProtectedRange().remove(newheadinit);
      }
    }
  }

  private static void insertBlockBefore(ControlFlowGraph graph, BasicBlock oldBlock, BasicBlock newBlock) {
    List<BasicBlock> lstTemp = new ArrayList<>();
    lstTemp.addAll(oldBlock.getPreds());
    lstTemp.addAll(oldBlock.getPredExceptions());

    // replace predecessors
    for (BasicBlock pred : lstTemp) {
      pred.replaceSuccessor(oldBlock, newBlock);
    }

    // copy exception edges and extend protected ranges
    for (BasicBlock hd : oldBlock.getSuccExceptions()) {
      newBlock.addSuccessorException(hd);

      ExceptionRangeCFG range = graph.getExceptionRange(hd, oldBlock);
      range.getProtectedRange().add(newBlock);
    }

    // replace handler
    for (ExceptionRangeCFG range : graph.getExceptions()) {
      if (range.getHandler() == oldBlock) {
        range.setHandler(newBlock);
      }
    }

    newBlock.addSuccessor(oldBlock);
    graph.getBlocks().addWithKey(newBlock, newBlock.id);
    if (graph.getFirst() == oldBlock) {
      graph.setFirst(newBlock);
    }
  }

  private static Set<BasicBlock> getAllBasicBlocks(Statement stat) {
    List<Statement> lst = new LinkedList<>();
    lst.add(stat);

    int index = 0;
    do {
      Statement st = lst.get(index);

      if (st instanceof BasicBlockStatement) {
        index++;
      } else {
        lst.addAll(st.getStats());
        lst.remove(index);
      }
    }
    while (index < lst.size());

    Set<BasicBlock> res = new LinkedHashSet<>();

    for (Statement st : lst) {
      res.add(((BasicBlockStatement) st).getBlock());
    }

    return res;
  }

  private boolean verifyFinallyEx(ControlFlowGraph graph, CatchAllStatement fstat, Record information) {
    Set<BasicBlock> tryBlocks = getAllBasicBlocks(fstat.getFirst());
    Set<BasicBlock> catchBlocks = getAllBasicBlocks(fstat.getHandler());

    FinallyType finallytype = information.finallyType;
    Map<BasicBlock, ExitType> mapLast = information.mapLast();

    BasicBlock first = fstat.getHandler().getBasichead().getBlock();
    boolean skippedFirst = false;

    if (finallytype == FinallyType.EMPTY) {
      // empty finally
      removeExceptionInstructionsEx(first, 3, finallytype);

      if (mapLast.containsKey(first)) {
        graph.getFinallyExits().add(first);
      }

      return true;
    }

    if (first.getSeq().length() == 1 && finallytype != FinallyType.OTHER) {
      BasicBlock firstsuc = first.getSuccs().get(0);
      if (catchBlocks.contains(firstsuc)) {
        // Check if the first block is just a `<stack var> = <fake var>` assignment.
        first = firstsuc;
        skippedFirst = true;
      }
    }

    // identify start blocks
    Set<BasicBlock> startBlocks = new LinkedHashSet<>();
    for (BasicBlock block : tryBlocks) {
      startBlocks.addAll(block.getSuccs());
    }
    // throw in the try body will point directly to the dummy exit
    // so remove dummy exit
    startBlocks.remove(graph.getLast());
    startBlocks.removeAll(tryBlocks);

    List<Area> lstAreas = new ArrayList<>();
    Set<BasicBlock> sideExits = null;
    for (BasicBlock start : startBlocks) {

      @Nullable Area arr = this.compareSubgraphsEx(graph, start, catchBlocks, first, finallytype, mapLast, skippedFirst);
      if (arr == null) {
        return false;
      }

      lstAreas.add(arr);

      if (sideExits == null) {
        sideExits = arr.sideExits;
      } else {
        ValidationHelper.validateTrue(sideExits.equals(arr.sideExits), "Side exits are not equal");
      }
    }

    // delete areas
    for (Area area : lstAreas) {
      deleteArea(graph, area);
    }

    // INFO: empty basic blocks may remain in the graph!
    for (Entry<BasicBlock, ExitType> entry : mapLast.entrySet()) {
      BasicBlock last = entry.getKey();

      if (entry.getValue() == ExitType.IMPLICIT_EXIT) {
        removeExceptionInstructionsEx(last, 2, finallytype);
        graph.getFinallyExits().add(last);
      }
    }

    removeExceptionInstructionsEx(fstat.getHandler().getBasichead().getBlock(), 1, finallytype);

    return true;
  }

  private @Nullable Area compareSubgraphsEx(
    ControlFlowGraph graph,
    BasicBlock startSample,
    Set<BasicBlock> catchBlocks,
    BasicBlock startCatch,
    FinallyType finallytype,
    Map<BasicBlock, ExitType> mapLast,
    boolean skippedFirst) {

    record BlockStackEntry(
      BasicBlock blockCatch,
      BasicBlock blockSample,
      // TODO: correct handling (merging) of multiple paths
      List<int[]> lstStoreVars) {
      BlockStackEntry {
        lstStoreVars = new ArrayList<>(lstStoreVars);
      }
    }

    List<BlockStackEntry> stack = new LinkedList<>();

    Set<BasicBlock> setSample = new HashSet<>();

    Map<Pair<BasicBlock, BasicBlock>, FinallyExit> mapNext = new LinkedHashMap<>();

    stack.add(new BlockStackEntry(startCatch, startSample, new ArrayList<>()));

    BasicBlock implicitNext = null;

    while (!stack.isEmpty()) {

      BlockStackEntry entry = stack.remove(0);
      BasicBlock blockCatch = entry.blockCatch;
      BasicBlock blockSample = entry.blockSample;

      boolean isFirstBlock = !skippedFirst && blockCatch == startCatch;
      @Nullable ExitType exitType = mapLast.get(blockCatch);  // null if not an exit

      if (!this.compareBasicBlocksEx(
        graph,
        blockCatch,
        blockSample,
        isFirstBlock,
        mapLast.get(blockCatch) == ExitType.IMPLICIT_EXIT,
        finallytype,
        entry.lstStoreVars)) {
        return null;
      }

      if (blockSample.getSuccs().size() != blockCatch.getSuccs().size()) {
        return null;
      }

      setSample.add(blockSample);

      // direct successors
      for (int i = 0; i < blockCatch.getSuccs().size(); i++) {
        BasicBlock sucCatch = blockCatch.getSuccs().get(i);
        BasicBlock sucSample = blockSample.getSuccs().get(i);

        if (catchBlocks.contains(sucCatch)) {
          if (!setSample.contains(sucSample)) {
            stack.add(new BlockStackEntry(sucCatch, sucSample, entry.lstStoreVars));
          }
        } else {
          if (exitType == ExitType.EXPLICIT_EXIT) {
            mapNext.put(Pair.of(blockSample, sucSample), new FinallyExit(blockSample, sucSample, exitType));
          } else if (exitType == ExitType.IMPLICIT_EXIT) {
            mapNext.put(Pair.of(blockSample, sucSample), new FinallyExit(blockSample, sucSample, exitType));
            if (implicitNext == null) {
              implicitNext = sucSample;
            } else if (implicitNext != sucSample){
              // Exits don't match
              return null;
            }
          }
        }
      }

      // exception successors
      if (exitType != null && blockSample.getSeq().isEmpty()) {
        // do nothing, blockSample will be removed anyway
        continue;
      } else if (blockCatch.getSuccExceptions().size() != blockSample.getSuccExceptions().size()) {
        return null;
      }

      for (int i = 0; i < blockCatch.getSuccExceptions().size(); i++) {
        BasicBlock sucCatch = blockCatch.getSuccExceptions().get(i);
        BasicBlock sucSample = blockSample.getSuccExceptions().get(i);

        String excCatch = graph.getExceptionRange(sucCatch, blockCatch).getUniqueExceptionsString();
        String excSample = graph.getExceptionRange(sucSample, blockSample).getUniqueExceptionsString();

        // FIXME: compare handlers if possible
        if (!Objects.equals(excCatch, excSample)) {
          return null;
        }

        if (catchBlocks.contains(sucCatch) && !setSample.contains(sucSample)) {
          List<int[]> lst = entry.lstStoreVars;

          // Add variable mapping for catch vars?
          if (!sucCatch.getSeq().isEmpty() && !sucSample.getSeq().isEmpty()) {
            Instruction instrCatch = sucCatch.getSeq().getInstr(0);
            Instruction instrSample = sucSample.getSeq().getInstr(0);

            if (instrCatch.opcode == CodeConstants.opc_astore &&
              instrSample.opcode == CodeConstants.opc_astore) {
              lst = new ArrayList<>(lst);
              lst.add(new int[]{instrCatch.operand(0), instrSample.operand(0)});
            }
          }

          stack.add(new BlockStackEntry(sucCatch, sucSample, lst));
        }
      }
    }

    return new Area(
      startSample,
      setSample,
      getUniqueNext(graph, new HashSet<>(mapNext.values())),
      getExplicitExits(mapNext.values()));
  }

  private static Set<BasicBlock> getExplicitExits(Collection<FinallyExit> setNext) {
    Set<BasicBlock> set = new HashSet<>();

    for (FinallyExit next : setNext) {
      if (next.type() == ExitType.EXPLICIT_EXIT) {
        set.add(next.succ());
      }
    }

    return set;
  }

  private static @Nullable BasicBlock getUniqueNext(ControlFlowGraph graph, Set<FinallyExit> setNext) {
    // precondition: there is at most one true exit path in a "finally" statement

    BasicBlock next = null;
    boolean multiple = false;

    for (FinallyExit arr : setNext) {

      if (arr.type() == ExitType.IMPLICIT_EXIT) {
        return arr.succ();
      }

      if (next == null) {
        next = arr.succ();
      } else if (next != arr.succ()) {
        multiple = true;
      }

      if (arr.succ().getPreds().size() == 1) {
        next = arr.succ();
      }
    }

    if (!multiple) {
      return next;
    }

    // TODO: generic solution
    for (FinallyExit arr : setNext) {
      BasicBlock block = arr.succ();

      if (block == next) {
        continue;
      }

      if (!InterpreterUtil.equalSets(next.getSuccs(), block.getSuccs())) {
        return null;
      }

      InstructionSequence seqNext = next.getSeq();
      InstructionSequence seqBlock = block.getSeq();

      if (seqNext.length() != seqBlock.length()) {
        return null;
      }
      for (int i = 0; i < seqNext.length(); i++) {
        // TODO: can this be merged with the methods to check if instructions are equal?
        Instruction instrNext = seqNext.getInstr(i);
        Instruction instrBlock = seqBlock.getInstr(i);

        if (!Instruction.equals(instrNext, instrBlock)) {
          return null;
        }
        for (int j = 0; j < instrNext.operandsCount(); j++) {
          if (instrNext.operand(j) != instrBlock.operand(j)) {
            return null;
          }
        }
      }
    }

    for (FinallyExit arr : setNext) {
      if (arr.succ() != next) {
        // FIXME: exception edge possible?
        arr.source().removeSuccessor(arr.succ());
        arr.source().addSuccessor(next);
      }
    }

    DeadCodeHelper.removeDeadBlocks(graph);

    return next;
  }

  private boolean compareBasicBlocksEx(
    ControlFlowGraph graph,
    BasicBlock pattern,
    BasicBlock sample,
    boolean isFirstBlock,
    boolean isTrueLastBlock,
    FinallyType finallytype,
    List<int[]> lstStoreVars) {

    InstructionSequence seqPattern = pattern.getSeq();
    InstructionSequence seqSample = sample.getSeq();

    if (isFirstBlock) { // first
      if (finallytype != FinallyType.OTHER) {
        seqPattern = seqPattern.subSequence(1); // drop first instruction
      }
    }

    if (isTrueLastBlock) { // last
      switch (finallytype) {
        case OTHER -> seqPattern = seqPattern.subSequence(0, -1);  // drop last
        case STORE -> seqPattern = seqPattern.subSequence(0, -2);  // drop last 2 // TODO: this is not always correct
      }
    }

    // Sample is allowed to be longer, in that case it is split into 2 basic blocks.
    //  TODO: does this make sense for cases where the current block isn't an IMPLICIT_END (trueLastBlock)?
    if (seqPattern.length() > seqSample.length()) {
      return false;
    }

    for (int i = 0; i < seqPattern.length(); i++) {
      Instruction instrPattern = seqPattern.getInstr(i);
      Instruction instrSample = seqSample.getInstr(i);

      // compare instructions with respect to jumps and variables.
      if (!this.equalInstructions(instrPattern, instrSample, lstStoreVars)) {
        return false;
      }
    }

    if (seqPattern.length() < seqSample.length()) { // split in two blocks
      splitBasicBlock(graph, sample, seqSample, seqPattern.length());
    }

    return true;
  }

  private static void splitBasicBlock(
    ControlFlowGraph graph,
    BasicBlock block,
    InstructionSequence seqSample,
    int splitIndex) {

    InstructionSequence seq = seqSample.split(splitIndex);
    List<Integer> oldOffsets = block.getInstrOldOffsets();
    List<Integer> offsetSublist = oldOffsets.subList(Math.min(splitIndex, oldOffsets.size()), oldOffsets.size());

    BasicBlock newBlock = new BasicBlock(++graph.last_id);
    newBlock.setSeq(seq);
    newBlock.getInstrOldOffsets().addAll(offsetSublist);
    offsetSublist.clear(); // remove offsets from original block

    List<BasicBlock> lstTemp = new ArrayList<>(block.getSuccs());

    // move successors
    for (BasicBlock suc : lstTemp) {
      block.removeSuccessor(suc);
      newBlock.addSuccessor(suc);
    }

    block.addSuccessor(newBlock);

    graph.getBlocks().addWithKey(newBlock, newBlock.id);

    Set<BasicBlock> setFinallyExits = graph.getFinallyExits();
    if (setFinallyExits.contains(block)) {
      setFinallyExits.remove(block);
      setFinallyExits.add(newBlock);
    }

    // copy exception edges and extend protected ranges
    for (int j = 0; j < block.getSuccExceptions().size(); j++) {
      BasicBlock hd = block.getSuccExceptions().get(j);
      newBlock.addSuccessorException(hd);

      ExceptionRangeCFG range = graph.getExceptionRange(hd, block);
      range.getProtectedRange().add(newBlock);
    }
  }

  public boolean equalInstructions(Instruction first, Instruction second, List<int[]> lstStoreVars) {
    ValidationHelper.notNull(this.instrRewrites);
    ValidationHelper.notNull(this.ssuversions);
    if (!Instruction.equals(first, second)) {
      return false;
    }

    if (first.group == CodeConstants.GROUP_JUMP || first.group == CodeConstants.GROUP_SWITCH) {
      // FIXME: switch comparison
      return true;
    }

    for (int i = 0; i < first.operandsCount(); i++) {
      int firstOp = first.operand(i);
      int secondOp = second.operand(i);
      if (firstOp != secondOp) {
        // a-load/store instructions
        if (first.opcode == CodeConstants.opc_aload) {
          for (int[] arr : lstStoreVars) {
            if (arr[0] == firstOp && arr[1] == secondOp) {
              return true;
            }
          }
        } else if (first.opcode == CodeConstants.opc_astore) {
          lstStoreVars.add(new int[]{firstOp, secondOp});
          return true;
        }

        boolean ok = false;
        if (isOpcVar(first.opcode)) {
          // Find rewritten variables
          if (this.instrRewrites.containsKey(first)) {
            firstOp = this.instrRewrites.get(first);
          }
          if (this.instrRewrites.containsKey(second)) {
            secondOp = this.instrRewrites.get(second);
          }

          if (this.ssuversions.areVarsAnalogous(firstOp, secondOp)) {
            ok = true;
          }

          // TODO: validate direct assignments
        }

        if (!ok) {
          return false;
        }
      }
    }

    return true;
  }

  private static boolean isOpcVar(int opc) {
    return (opc >= CodeConstants.opc_iload && opc <= CodeConstants.opc_sastore) || opc == CodeConstants.opc_iinc;
  }

  private static void deleteArea(ControlFlowGraph graph, Area area) {
    BasicBlock start = area.start;
    BasicBlock next = area.next;

    if (start == next) {
      return;
    }

    if (next == null) {
      // dummy exit block
      next = graph.getLast();
    }

    // collect common exception ranges of predecessors and successors
    Set<BasicBlock> setCommonExceptionHandlers = new HashSet<>(next.getSuccExceptions());
    for (BasicBlock pred : start.getPreds()) {
      setCommonExceptionHandlers.retainAll(pred.getSuccExceptions());
    }

    boolean is_outside_range = false;

    Set<BasicBlock> setPredecessors = new HashSet<>(start.getPreds());

    // replace start with next
    for (BasicBlock pred : setPredecessors) {
      pred.replaceSuccessor(start, next);
    }

    Set<BasicBlock> setBlocks = area.sample;

    Set<ExceptionRangeCFG> setCommonRemovedExceptionRanges = null;

    // remove all the blocks inbetween
    for (BasicBlock block : setBlocks) {

      // artificial basic blocks (those resulted from splitting)
      // can belong to more than one area
      if (graph.getBlocks().containsKey(block.id)) {

        if (!block.getSuccExceptions().containsAll(setCommonExceptionHandlers)) {
          is_outside_range = true;
        }

        Set<ExceptionRangeCFG> setRemovedExceptionRanges = new HashSet<>();
        for (BasicBlock handler : block.getSuccExceptions()) {
          setRemovedExceptionRanges.add(graph.getExceptionRange(handler, block));
        }

        if (setCommonRemovedExceptionRanges == null) {
          setCommonRemovedExceptionRanges = setRemovedExceptionRanges;
        } else {
          setCommonRemovedExceptionRanges.retainAll(setRemovedExceptionRanges);
        }

        // shift extern edges on splitted blocks
        if (block.getSeq().isEmpty() && block.getSuccs().size() == 1) {
          BasicBlock succs = block.getSuccs().get(0);
          for (BasicBlock pred : new ArrayList<>(block.getPreds())) {
            if (!setBlocks.contains(pred)) {
              pred.replaceSuccessor(block, succs);
            }
          }

          if (graph.getFirst() == block) {
            graph.setFirst(succs);
          }
        }

        graph.removeBlock(block);
      }
    }

    if (is_outside_range) {
      // new empty block
      BasicBlock emptyblock = new BasicBlock(++graph.last_id);

      graph.getBlocks().addWithKey(emptyblock, emptyblock.id);

      // add to ranges if necessary
      for (ExceptionRangeCFG range : setCommonRemovedExceptionRanges) {
        emptyblock.addSuccessorException(range.getHandler());
        range.getProtectedRange().add(emptyblock);
      }

      // insert between predecessors and next
      emptyblock.addSuccessor(next);
      for (BasicBlock pred : setPredecessors) {
        pred.replaceSuccessor(next, emptyblock);
      }
    }
  }

  private static void removeExceptionInstructionsEx(BasicBlock block, int blocktype, FinallyType finallytype) {
    InstructionSequence seq = block.getSeq();
    List<Integer> instrOldOffsets = block.getInstrOldOffsets();

    if (finallytype == FinallyType.EMPTY) { // empty finally handler
      for (int i = seq.length() - 1; i >= 0; i--) {
        seq.removeInstruction(i);
      }
    } else {
      if ((blocktype & 1) > 0) { // first
        if (finallytype == FinallyType.STORE || finallytype == FinallyType.DROP) { // astore or pop
          seq.removeInstruction(0);
        }
      }

      if ((blocktype & 2) > 0) { // last
        if (finallytype == FinallyType.STORE || finallytype == FinallyType.DROP) { // astore or pop
          seq.removeLast();
        }

        if (finallytype == FinallyType.STORE) { // astore
          seq.removeLast();
        }
      }
    }
  }

  private static final int[] STORE_CODES = {CodeConstants.opc_istore, CodeConstants.opc_lstore, CodeConstants.opc_fstore, CodeConstants.opc_dstore, CodeConstants.opc_astore};
  private static final int[][] NEXT_CODES = {
    {CodeConstants.opc_iload, CodeConstants.opc_ireturn},
    {CodeConstants.opc_lload, CodeConstants.opc_lreturn},
    {CodeConstants.opc_fload, CodeConstants.opc_freturn},
    {CodeConstants.opc_dload, CodeConstants.opc_dreturn},
    {CodeConstants.opc_aload, CodeConstants.opc_areturn}
  };

  // Try to inline
  //
  // istore <v>
  // (successor)
  // iload <v>
  // ireturn
  //
  // into the predecessor
  private static void inlineReturnVar(ControlFlowGraph graph, BasicBlock handler, Record inf) {
    List<ExceptionRangeCFG> ranges = new ArrayList<>();

    // Find all exception ranges with this finally block as the handler
    for (ExceptionRangeCFG ex : graph.getExceptions()) {
      if (ex.getHandler() == handler) {
        ranges.add(ex);
      }
    }

    Set<BasicBlock> exits = new HashSet<>();

    for (ExceptionRangeCFG ex : ranges) {
      // For each range, find the exit blocks
      for (BasicBlock block : ex.getProtectedRange()) {
        List<BasicBlock> blockEx = block.getSuccExceptions();

        for (BasicBlock suc : block.getSuccs()) {
          // Leaving the exception handler
          if (!suc.getSuccExceptions().equals(blockEx)) {
            exits.add(block);
          }
        }
      }
    }

    for (BasicBlock exit : exits) {
      // We only want exits with 1 successor block
      if (exit.getSuccs().size() == 1 && !exit.getSeq().isEmpty()) {
        Instruction instr = exit.getLastInstruction();

        int index = indexOf(instr);

        if (index >= 0) {

          BasicBlock succ = exit.getSuccs().get(0);
          if (succ.getPreds().size() != 1 || succ.getPredExceptions().size() != 0) {
            continue;
          }

          InstructionSequence nextSeq = succ.getSeq();
          // Returns in finally blocks are always placed before the exception block
          if (nextSeq.length() == 2 && nextSeq.getInstr(0).startOffset < inf.exceptionOffset) {
            // Check if next block's sequence is load and return
            if (nextSeq.getInstr(0).opcode == NEXT_CODES[index][0] && nextSeq.getInstr(1).opcode == NEXT_CODES[index][1]) {
              // Make sure variable index is correct
              if (instr.operand(0) == nextSeq.getInstr(0).operand(0)) {
                // remove store
                exit.getSeq().removeLast();
                // add return
                exit.getSeq().addInstruction(nextSeq.getInstr(1));

                // Clear next exception range, mergeBasicBlocks will take care of it
                nextSeq.clear();
              }
            }
          }
        }
      }
    }
  }

  private static int indexOf(Instruction instr) {
    for (int i = 0; i < STORE_CODES.length; i++) {
      int code = STORE_CODES[i];

      if (instr.opcode == code) {
        return i;
      }
    }

    return -1;
  }
}
