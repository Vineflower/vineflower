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

  public void verifyTypes(GenericClassDescriptor descriptor, List<VarType> realParamTypes, VarType realReturnType, List<VarType> realExceptionTypes) {
    GenericsChecker checker = descriptor == null ? new GenericsChecker(typeParameters, typeParameterBounds) : descriptor.getChecker().copy(typeParameters, typeParameterBounds);

    for (int i = 0; i < parameterTypes.size(); i++) {
      VarType parameterType = parameterTypes.get(i);
      VarType actualType = realParamTypes.get(i);

      if (!checker.isProperlyBounded(parameterType, actualType)) {
        DecompilerContext.getLogger().writeMessage("Mismatched method parameter signature, expected: " + actualType.value + ", actual: " + parameterType.value, IFernflowerLogger.Severity.WARN);
        parameterTypes.set(i, actualType);
      }
    }

    if (!checker.isProperlyBounded(returnType, realReturnType)) {
      DecompilerContext.getLogger().writeMessage("Mismatched method parameter signature, expected: " + realReturnType.value + ", actual: " + returnType.value, IFernflowerLogger.Severity.WARN);
      returnType = realReturnType;
    }
  }
}