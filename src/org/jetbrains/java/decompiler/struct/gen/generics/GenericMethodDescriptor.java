// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.gen.generics;

import java.util.Collections;
import java.util.List;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class GenericMethodDescriptor {
  public final List<String> typeParameters;
  public final List<List<VarType>> typeParameterBounds;
  public final List<VarType> parameterTypes;
  public VarType returnType;
  public final List<VarType> exceptionTypes;

  public GenericMethodDescriptor(List<String> typeParameters,
                                 List<List<VarType>> typeParameterBounds,
                                 List<VarType> parameterTypes,
                                 VarType returnType,
                                 List<VarType> exceptionTypes) {
    this.typeParameters = substitute(typeParameters);
    this.typeParameterBounds = substitute(typeParameterBounds);
    this.parameterTypes = substitute(parameterTypes);
    this.returnType = returnType;
    this.exceptionTypes = substitute(exceptionTypes);
  }

  private static <T> List<T> substitute(List<T> list) {
    return list.isEmpty() ? Collections.emptyList() : list;
  }

  public void verifyTypes(GenericsChecker checker, List<VarType> realParamTypes, VarType realReturnType, List<VarType> realExceptionTypes) {
    checker = checker == null ? new GenericsChecker(typeParameters, typeParameterBounds) : checker.copy(typeParameters, typeParameterBounds);

    for (int i = 0; i < parameterTypes.size(); i++) {
      VarType parameterType = parameterTypes.get(i);
      VarType realType = realParamTypes.get(i);

      if (!checker.isProperlyBounded(parameterType, realType)) {
        DecompilerContext.getLogger().writeMessage("Mismatched method parameter signature, expected: " + realType.value + ", actual: " + parameterType.value, IFernflowerLogger.Severity.WARN);
        parameterTypes.set(i, realType);
      }
    }

    if (!checker.isProperlyBounded(returnType, realReturnType)) {
      DecompilerContext.getLogger().writeMessage("Mismatched method return signature, expected: " + realReturnType.value + ", actual: " + returnType.value, IFernflowerLogger.Severity.WARN);
      returnType = realReturnType;
    }
    
    for (int i = 0; i < exceptionTypes.size(); i++) {
      VarType exceptionType = exceptionTypes.get(i);
      VarType realType = realExceptionTypes.get(i);
      if (!checker.isProperlyBounded(exceptionType, realType)) {
        DecompilerContext.getLogger().writeMessage("Mismatched method exception signature, expected: " + realType.value + ", actual: " + exceptionType.value, IFernflowerLogger.Severity.WARN);
      }
    }
  }
}