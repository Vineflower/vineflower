// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.gen.generics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class GenericClassDescriptor {

  public VarType superclass;

  public GenericType genericType;

  public final List<VarType> superinterfaces = new ArrayList<>();

  public final List<String> fparameters = new ArrayList<>();

  public final List<List<VarType>> fbounds = new ArrayList<>();

  private GenericsChecker checker;

  public GenericsChecker getChecker() {
    if (checker == null) {
      checker = new GenericsChecker(fparameters, fbounds);
    }

    return checker;
  }

  public void verifyTypes(VarType actualSuperclass, List<VarType> actualSuperInterfaces) {
    GenericsChecker checker = getChecker();

    if (superclass != null) {
      if (!checker.isProperlyBounded(actualSuperclass, superclass)) {
        DecompilerContext.getLogger().writeMessage("Mismatched superclass signature, expected: " + actualSuperclass + ", actual: " + superclass.value, IFernflowerLogger.Severity.WARN);
        superclass = actualSuperclass;
      }
    }

    for (int i = 0; i < superinterfaces.size(); i++) {
      VarType actualSuperInterface = i < actualSuperInterfaces.size() ? actualSuperInterfaces.get(i) : null;
      VarType superInterface = superinterfaces.get(i);

      if (actualSuperInterface == null) {
        DecompilerContext.getLogger().writeMessage("Actual superinterface count is less than expected", IFernflowerLogger.Severity.WARN);
        break;
      }

      if (!checker.isProperlyBounded(actualSuperInterface, superInterface)) {
        DecompilerContext.getLogger().writeMessage("Mismatched superinterface signature, expected: " + actualSuperInterface + ", actual: " + superInterface.value, IFernflowerLogger.Severity.WARN);
        superinterfaces.set(i, actualSuperInterface);
      }
    }
  }
}
