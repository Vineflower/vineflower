// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.sforms;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.FlattenStatementsHelper.FinallyPathWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionEdge;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionNode;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsGraph;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.*;
import org.jetbrains.java.decompiler.util.FastSparseSetFactory.FastSparseSet;

import java.util.*;

import static org.jetbrains.java.decompiler.modules.decompiler.sforms.VarMapHolder.mergeMaps;

public class SSAConstructorSparseEx extends SSAProcessor {

  public SSAConstructorSparseEx(){
    super(
      false,
      true,
      false,
      false,
      false,
      false,
      false,
      false,
      false,
      false
    );
  }
}
