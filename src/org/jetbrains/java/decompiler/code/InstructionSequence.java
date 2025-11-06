// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.code;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.util.TextUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class InstructionSequence implements Iterable<Instruction> {
  private final List<Instruction> instructions;

  public InstructionSequence() {
    this(new ArrayList<>());
  }

  public InstructionSequence(Collection<Instruction> instructions) {
    this.instructions = new ArrayList<>(instructions);
  }

  // *****************************************************************************
  // public methods
  // *****************************************************************************

  @Override
  public InstructionSequence clone() {
    return new InstructionSequence(this.instructions); // Constructor takes a copy
  }

  public void clear() {
    this.instructions.clear();
  }

  public void addInstruction(Instruction inst) {
    this.instructions.add(inst);
  }

  public void addInstruction(int index, Instruction inst) {
    this.instructions.add(index, inst);
  }

  public void addSequence(InstructionSequence seq) {
    this.instructions.addAll(seq.instructions);
  }

  public void removeInstruction(int index) {
    this.instructions.remove(index);
  }

  public void removeLast() {
    if (!this.instructions.isEmpty()) {
      this.instructions.remove(this.instructions.size() - 1);
    }
  }

  public Instruction getInstr(int index) {
    return this.instructions.get(index);
  }

  public Instruction getLastInstr() {
    return this.instructions.get(this.instructions.size() - 1);
  }

  public int length() {
    return this.instructions.size();
  }

  public boolean isEmpty() {
    return this.instructions.isEmpty();
  }

  public String toString() {
    return toString(0);
  }

  public String toString(int indent) {

    String new_line_separator = DecompilerContext.getNewLineSeparator();

    StringBuilder buf = new StringBuilder();

    for (var instr : this.instructions) {
      buf.append(TextUtil.getIndentString(indent));
      buf.append(instr.startOffset);
      buf.append(": ");
      buf.append(instr);
      buf.append(new_line_separator);
    }

    return buf.toString();
  }

  // *****************************************************************************
  // getter and setter methods
  // *****************************************************************************

  @Override
  public Iterator<Instruction> iterator() {
    return this.instructions.iterator();
  }
}