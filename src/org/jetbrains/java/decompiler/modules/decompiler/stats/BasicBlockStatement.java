// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.stats;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.Instruction;
import org.jetbrains.java.decompiler.code.InstructionSequence;
import org.jetbrains.java.decompiler.code.SimpleInstructionSequence;
import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Pattern;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.util.StartEndPair;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.ArrayList;
import java.util.List;

public final class BasicBlockStatement extends Statement {

  // *****************************************************************************
  // private fields
  // *****************************************************************************

  private final BasicBlock block;
  private boolean removableMonitorexit;

  // *****************************************************************************
  // constructors
  // *****************************************************************************

  public BasicBlockStatement(BasicBlock block) {

    super(StatementType.BASIC_BLOCK, block.id);

    this.block = block;

    CounterContainer coun = DecompilerContext.getCounterContainer();
    if (id >= coun.getCounter(CounterContainer.STATEMENT_COUNTER)) {
      coun.setCounter(CounterContainer.STATEMENT_COUNTER, id + 1);
    }

    Instruction instr = block.getLastInstruction();
    if (instr != null) {
      if (instr.group == CodeConstants.GROUP_JUMP && instr.opcode != CodeConstants.opc_goto) {
        lastBasicType = LastBasicType.IF;
      }
      else if (instr.group == CodeConstants.GROUP_SWITCH) {
        lastBasicType = LastBasicType.SWITCH;
      }
    }

    // monitorenter and monitorexits
    buildMonitorFlags();
  }

  @Override
  public void markMonitorexitDead() {
    InstructionSequence seq = this.getBlock().getSeq();

    if (seq != null && !seq.isEmpty()) {
      for (int i = 0; i < seq.length(); i++) {
        if (seq.getInstr(i).opcode == CodeConstants.opc_monitorexit) {
          this.setRemovableMonitorexit(true);
          break;
        }
      }
    }
  }

  @Override
  public BasicBlockStatement getBasichead() {
    return this;
  }

  // *****************************************************************************
  // public methods
  // *****************************************************************************

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer tb = ExprProcessor.listToJava(varDefinitions, indent);
    boolean islabeled = isLabeled();
    if (islabeled) {
      tb.appendIndent(indent).append("label").append(id).append(':').append("// $VF: Invalid label!").appendLineSeparator();
    }

    tb.append(ExprProcessor.listToJava(exprents, indent));
    return tb;
  }

  @Override
  public Statement getSimpleCopy() {

    BasicBlock newblock = new BasicBlock(
      DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.STATEMENT_COUNTER));

    SimpleInstructionSequence seq = new SimpleInstructionSequence();
    for (int i = 0; i < block.getSeq().length(); i++) {
      seq.addInstruction(block.getSeq().getInstr(i).clone(), -1);
    }

    newblock.setSeq(seq);

    return new BasicBlockStatement(newblock);
  }

  // TODO: cache this?
  @Override
  public List<VarExprent> getImplicitlyDefinedVars() {
    if (getExprents() != null && getExprents().size() > 0) {
      List<VarExprent> vars = new ArrayList<>();
      List<Exprent> exps = getExprents();

      for (Exprent exp : exps) {
        List<Exprent> inner = exp.getAllExprents(true);
        inner.add(exp);

        for (Exprent exprent : inner) {
          if (exprent instanceof FunctionExprent func && func.getFuncType() == FunctionExprent.FunctionType.INSTANCEOF) {
            if (func.getLstOperands().size() > 2) {
              vars.addAll(((Pattern) func.getLstOperands().get(2)).getPatternVars());
            }
          }
        }
      }

      return vars;
    }

    return null;
  }

  public boolean isRemovableMonitorexit() {
    return removableMonitorexit;
  }

  public void setRemovableMonitorexit(boolean removableMonitorexit) {
    this.removableMonitorexit = removableMonitorexit;
  }

  public static BasicBlockStatement create() {
    BasicBlockStatement stat = new BasicBlockStatement(new BasicBlock(DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.STATEMENT_COUNTER)));
    stat.setExprents(new ArrayList<>());

    return stat;
  }

  @Override
  public boolean hasBasicSuccEdge() {
    return true;
  }

  // *****************************************************************************
  // getter and setter methods
  // *****************************************************************************

  public BasicBlock getBlock() {
    return block;
  }

  // TODO: is this allowed? SecondaryFunctionsHelper says "only head expressions can be replaced!"
  @Override
  public void replaceExprent(Exprent oldexpr, Exprent newexpr) {
    for (int i = 0; i < this.exprents.size(); i++) {
      if (this.exprents.get(i) == oldexpr) {
        this.exprents.set(i, newexpr);
      }
    }
  }

  @Override
  public StartEndPair getStartEndRange() {
    if (block.size() > 0) {
      return new StartEndPair(block.getStartInstruction(), block.getEndInstruction());
    } else {
      return new StartEndPair(0, 0);
    }
  }
}
