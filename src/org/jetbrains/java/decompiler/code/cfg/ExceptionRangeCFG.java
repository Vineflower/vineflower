// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.code.cfg;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.main.DecompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExceptionRangeCFG {
  private final @NotNull List<BasicBlock> protectedRange; // FIXME: replace with set
  private @NotNull BasicBlock handler;
  private final @NotNull List<String> exceptionTypes;

  public ExceptionRangeCFG(@NotNull List<BasicBlock> protectedRange, @NotNull BasicBlock handler, @NotNull List<String> exceptionType) {
    this.protectedRange = protectedRange;
    this.handler = handler;

    this.exceptionTypes = new ArrayList<>(exceptionType);
  }

  public ExceptionRangeCFG(@NotNull List<BasicBlock> protectedRange, @NotNull BasicBlock handler, @NotNull String exceptionType) {
    this.protectedRange = protectedRange;
    this.handler = handler;

    this.exceptionTypes = new ArrayList<>();
    this.exceptionTypes.add(exceptionType);
  }

  public boolean isCircular() {
    return protectedRange.contains(handler);
  }

  @Override
  public String toString() {
    String new_line_separator = DecompilerContext.getNewLineSeparator();
    StringBuilder buf = new StringBuilder();

    buf.append("exceptionType:");

    for (String exception_type : exceptionTypes) {
      buf.append(" ").append(exception_type);
    }

    buf.append(new_line_separator);

    buf.append("handler: ").append(handler.getId()).append(new_line_separator);
    buf.append("range: ");
    for (BasicBlock block : protectedRange) {
      buf.append(block.getId()).append(" ");
    }
    buf.append(new_line_separator);

    return buf.toString();
  }

  public @NotNull BasicBlock getHandler() {
    return handler;
  }

  public void setHandler(@NotNull BasicBlock handler) {
    this.handler = handler;
  }

  public @NotNull List<BasicBlock> getProtectedRange() {
    return protectedRange;
  }

  public @NotNull List<String> getExceptionTypes() {
    return this.exceptionTypes;
  }

  public void addExceptionType(@NotNull String exceptionType) {
    this.exceptionTypes.add(exceptionType);
  }

  public @NotNull String getUniqueExceptionsString() {
    return exceptionTypes.stream().distinct().collect(Collectors.joining(":"));
  }
}