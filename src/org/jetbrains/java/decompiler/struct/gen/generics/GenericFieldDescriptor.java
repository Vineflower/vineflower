// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.gen.generics;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class GenericFieldDescriptor {
  public VarType type;

  public GenericFieldDescriptor(VarType type) {
    this.type = type;
  }

  public void verifyType(GenericClassDescriptor containingClassGenerics, VarType realType) {
    if (containingClassGenerics == null) {
//      DecompilerContext.getLogger().writeMessage("Class generics were not found, verifying type loosely", IFernflowerLogger.Severity.INFO);
      verifyLoosely(realType);
      return;
    }

    GenericsChecker checker = containingClassGenerics.getChecker();

    if (!checker.isProperlyBounded(type, realType)) {
      DecompilerContext.getLogger().writeMessage("Mismatched field signature, expected: " + realType.value + ", actual: " + type.value, IFernflowerLogger.Severity.WARN);
      type = realType;
    }
  }

  //FIXME this is necessary because some places don't have other signature information,
  // which prevents the ability to check class- or method-provided generic types
  public void verifyLoosely(VarType actualType) {
    if (actualType.higherEqualInLatticeThan(type)) {
      return;
    }

    if (type.type == CodeType.GENVAR) {
      return; // Impossible to verify
    }

    DecompilerContext.getLogger().writeMessage("Mismatched field signature, expected: " + type.value + ", actual: " + actualType.value, IFernflowerLogger.Severity.WARN);
    type = actualType;
  }
}