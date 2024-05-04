// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.code;

import org.jetbrains.java.decompiler.main.DecompilerContext;

public record ExceptionHandler(
  int from,
  int to,
  int handler,
  String exceptionClass
) {
  public String toString() {
    String newLineSeparator = DecompilerContext.getNewLineSeparator();
    return "from instr: " + this.from + " to instr: " + this.to + " handler instr: " + handler +
      newLineSeparator + "exception class: " + this.exceptionClass + newLineSeparator;
  }
}