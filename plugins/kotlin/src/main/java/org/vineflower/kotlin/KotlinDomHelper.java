package org.vineflower.kotlin;

import org.jetbrains.java.decompiler.api.GraphParser;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.Instruction;
import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.code.cfg.ControlFlowGraph;
import org.jetbrains.java.decompiler.modules.code.DeadCodeHelper;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.struct.StructMethod;

import java.util.HashSet;
import java.util.Set;

public class KotlinDomHelper implements GraphParser {
  /**
   * This is a prototype
   */

  private final GraphParser domHelper = new DomHelper();

  @Override
  public RootStatement createStatement(ControlFlowGraph graph, StructMethod mt) {
    var entry = this.isCoroutine(mt);
    if (entry >= 0) {
      this.fixCoroutineGraph(graph, entry);
    }

    return this.domHelper.createStatement(graph, mt);
  }

  private void fixCoroutineGraph(ControlFlowGraph graph, int continuationParamIndex) {
    // Step 1: check for the existence of the "coroutine preamble"

    removePreamble(graph, continuationParamIndex);
  }

  private void removePreamble(ControlFlowGraph graph, int continuationParamIndex) {
    var entryBlock = graph.getFirst();
    if (entryBlock == null || entryBlock.size() != 3 || entryBlock.getSuccs().size() != 2) {
      return;
    }

    var firstInst = entryBlock.getInstruction(0);

    if (firstInst.opcode != CodeConstants.opc_aload || firstInst.operand(0) != continuationParamIndex) {
      return;
    }

//    var ifInst = entryBlock.getInstruction(2);
//    if (ifInst.opcode != CodeConstants.opc_ifeq) {
//      return -1;
//    }

    // Now we just assume that the preamble is there

    // place where the new ContinuationObject is made
    var newBlock = entryBlock.getSuccs().get(0);
    //
    var innerIfBlock = entryBlock.getSuccs().get(1);

    if (innerIfBlock.getSuccs().size() != 2) {
      return;
    }


    var lastInst = newBlock.getLastInstruction();

    if (lastInst.opcode != CodeConstants.opc_astore) {
      return;
    }

    // This is the '$continuation' variable's index
    var storeIndex = lastInst.operand(0);

    // Now we need to remove the preamble
    if (newBlock.getSuccs().size() != 1) {
      return;
    }

    var switchBlock = newBlock.getSuccs().get(0);
    var case0Block = switchBlock.getSuccs().get(1);

    var resultIndex = switchBlock.getInstruction(2).operand(0);
    var suspendIndex = switchBlock.getInstruction(4).operand(0);

    graph.setFirst(case0Block);

    // Remove no longer needed instructions
    Set<BasicBlock> seen = new HashSet<>();

    // remove the ResultKt.throwOnFailure(`$result`) call
    if (case0Block.size() > 2) {
      var firstInst2 = case0Block.getInstruction(0);
      var secondInst2 = case0Block.getInstruction(1);

      if (firstInst2.opcode == CodeConstants.opc_aload && firstInst2.operand(0) == resultIndex &&
        secondInst2.opcode == CodeConstants.opc_invokestatic) {
        var instructions = case0Block.getSeq();
        instructions.removeInstruction(0);
        instructions.removeInstruction(0);
      }
    }

    removeInstructions(case0Block, storeIndex, suspendIndex, seen);

    DeadCodeHelper.removeDeadBlocks(graph);


  }

  private static void removeInstructions(BasicBlock currentBlock, int storeIndex, int suspendIndex, Set<BasicBlock> seen) {
    if (!seen.add(currentBlock)) {
      // Already seen
      return;
    }

    var instructions = currentBlock.getSeq();
    for (int i = 0; i < instructions.length(); i++) {
      var inst = instructions.getInstr(i);

      if (inst.opcode == CodeConstants.opc_aload) {
        if (inst.operand(0) == storeIndex) {
          if (i + 2 < instructions.length()) {
//              var loadVar = instructions.getInstr(i + 1);
            var storeField = instructions.getInstr(i + 2);

            if (storeField.opcode == CodeConstants.opc_putfield) {
              instructions.removeInstruction(i);
              instructions.removeInstruction(i);
              instructions.removeInstruction(i);

              //noinspection AssignmentToForLoopParameter
              i--; // the i'th instruction is now the next instruction
              continue;
            }
          }

          // load null instead, this is probably an arg to a suspend function
          instructions.removeInstruction(i);
          instructions.addInstruction(
            i,
            Instruction.create(CodeConstants.opc_aconst_null, false, CodeConstants.GROUP_GENERAL,
              inst.bytecodeVersion, new int[0], 1), -1);
        }
      }
    }

    if (instructions.length() > 2) {
      var lastInst = instructions.getInstr(instructions.length() - 1);
      var secondLastInst = instructions.getInstr(instructions.length() - 2);

      if (lastInst.opcode == CodeConstants.opc_if_acmpne &&
        secondLastInst.opcode == CodeConstants.opc_aload && secondLastInst.operand(0) == suspendIndex) {
        // Remove the last two instructions
        instructions.removeInstruction(instructions.length() - 1);
        instructions.removeInstruction(instructions.length() - 1);
        currentBlock.removeSuccessor(currentBlock.getSuccs().get(1)); // remove return block
      }
    }


    for (var succ : currentBlock.getSuccs()) {
      removeInstructions(succ, storeIndex, suspendIndex, seen);
    }

  }

  private int isCoroutine(StructMethod mt) {
    // TODO: make this an option?
    // TODO: don't fix the graph when a "suspendCoroutineUninterceptedOrReturn" intrinsic is used

    var params = mt.methodDescriptor().params;

    if (params.length == 0) {
      return -1;
    }

    var lastParam = params[params.length - 1];
    if (lastParam.type != CodeConstants.TYPE_OBJECT || !lastParam.value.equals("kotlin/coroutines/Continuation")) {
      return -1;
    }

    int index = params.length;

    // Is there already a method for this?
    for (var param : params) {
      if (param.type == CodeConstants.TYPE_LONG || param.type == CodeConstants.TYPE_DOUBLE) {
        index++;
      }
    }

    return index;
  }
}
