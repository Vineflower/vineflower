// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.code;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.util.TextUtil;

import java.util.*;


public record FullInstructionSequence(
  List<Instruction> instructions,
  Map<Integer, Integer> offsetToIndex,
  ExceptionTable exceptionTable
) implements Iterable<Instruction> {

  public Instruction getInstr(int index) {
    return this.instructions.get(index);
  }

  public int length() {
    return this.instructions.size();
  }

  public boolean isEmpty() {
    return this.instructions.isEmpty();
  }

  public int getIndexByRelOffset(Instruction instruction, int offset) {
    int absoluteOffset = instruction.startOffset + offset;
    return offsetToIndex.getOrDefault(absoluteOffset, -1);
  }

  @Override
  public Iterator<Instruction> iterator() {
    return this.instructions.iterator();
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

  public Instruction getLast() {
    return this.instructions.get(this.instructions.size() - 1);
  }
}
