/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.modules.decompiler.vars.CheckTypesResult;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class AssertExprent extends Exprent {

  private final List<Exprent> parameters;

  public AssertExprent(List<Exprent> parameters) {
    super(Type.ASSERT);
    this.parameters = parameters;
  }

  @Override
  protected List<Exprent> getAllExprents(List<Exprent> list) {
    List<Exprent> copy = new ArrayList<>(this.parameters);
    copy.removeIf(Objects::isNull);
    list.addAll(copy);
    return list;
  }

  @Override
  public Exprent copy() {
    return new AssertExprent(new ArrayList<>(parameters));
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buffer = new TextBuffer();

    buffer.append("assert ");

    buffer.addBytecodeMapping(bytecode);

    if (parameters.get(0) == null) {
      buffer.append("false");
    }
    else {
      buffer.append(parameters.get(0).toJava(indent));
    }

    if (parameters.size() > 1) {
      buffer.append(" : ");
      buffer.append(parameters.get(1).toJava(indent));
    }

    return buffer;
  }

  @Override
  public void replaceExprent(Exprent oldExpr, Exprent newExpr) {
    for (int i = 0; i < parameters.size(); i++) {
      if (oldExpr == parameters.get(i)) {
        parameters.set(i, newExpr);
      }
    }
  }

  @Override
  public CheckTypesResult checkExprTypeBounds() {
    // TODO: bool for param 0, str for param 1
    return super.checkExprTypeBounds();
  }

  @Override
  public void getBytecodeRange(BitSet values) {
    measureBytecode(values, parameters);
    measureBytecode(values);
  }
}
