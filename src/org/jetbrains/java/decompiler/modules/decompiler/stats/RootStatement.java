// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.stats;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.BytecodeMappingTracer;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.StartEndPair;

public class RootStatement extends Statement {
  private final DummyExitStatement dummyExit;
  public final StructMethod mt;

  public RootStatement(Statement head, DummyExitStatement dummyExit, StructMethod mt) {
    type = Statement.TYPE_ROOT;

    first = head;
    this.dummyExit = dummyExit;
    this.mt = mt;

    stats.addWithKey(first, first.id);
    first.setParent(this);
  }

  @Override
  public TextBuffer toJava(int indent, BytecodeMappingTracer tracer) {
    if (DecompilerContext.getOption(IFernflowerPreferences.STUB_MODE)) {
      TextBuffer buf = new TextBuffer();
      buf.appendIndent(indent);
      buf.append("throw new AbstractMethodError();");
      buf.appendLineSeparator();
      tracer.incrementCurrentSourceLine();
      return buf;
    }
    return ExprProcessor.listToJava(varDefinitions, indent, tracer).append(first.toJava(indent, tracer));
  }

  public DummyExitStatement getDummyExit() {
    return dummyExit;
  }

  @Override
  public StartEndPair getStartEndRange() {
    return StartEndPair.join(first.getStartEndRange(), dummyExit != null ? dummyExit.getStartEndRange() : null);
  }
}
