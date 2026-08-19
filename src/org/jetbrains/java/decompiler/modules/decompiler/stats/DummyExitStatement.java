// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.stats;

import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public final class DummyExitStatement extends Statement {
  public @Nullable BitSet bytecode = null;  // offsets of bytecode instructions mapped to dummy exit

  public DummyExitStatement() {
    super(StatementType.DUMMY_EXIT);
  }

  public void addBytecodeOffsets(@Nullable BitSet bytecodeOffsets) {
    if (bytecodeOffsets != null && !bytecodeOffsets.isEmpty()) {
      if (bytecode == null) {
        bytecode = new BitSet();
      }
      bytecode.or(bytecodeOffsets);
    }
  }
}
