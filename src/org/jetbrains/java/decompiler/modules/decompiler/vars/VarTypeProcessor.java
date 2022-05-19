// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.*;

public class VarTypeProcessor {
  public static final int VAR_NON_FINAL = 1;
  public static final int VAR_EXPLICIT_FINAL = 2;
  public static final int VAR_FINAL = 3;

  private final StructMethod method;
  private final MethodDescriptor methodDescriptor;
  private final Map<VarVersionPair, VarType> mapExprentMinTypes = new HashMap<>();
  private final Map<VarVersionPair, VarType> mapExprentMaxTypes = new HashMap<>();
  private final Map<VarVersionPair, Integer> mapFinalVars = new HashMap<>();

  public VarTypeProcessor(StructMethod mt, MethodDescriptor md) {
    method = mt;
    methodDescriptor = md;
  }

  public void calculateVarTypes(RootStatement root, DirectGraph graph) {
    setInitVars(root);

    resetExprentTypes(graph);

    //noinspection StatementWithEmptyBody
    while (!processVarTypes(graph)) ;
  }

  private void setInitVars(RootStatement root) {
    boolean thisVar = !method.hasModifier(CodeConstants.ACC_STATIC);

    MethodDescriptor md = methodDescriptor;

    if (thisVar) {
      StructClass cl = (StructClass)DecompilerContext.getProperty(DecompilerContext.CURRENT_CLASS);
      VarType clType = new VarType(CodeConstants.TYPE_OBJECT, 0, cl.qualifiedName);
      mapExprentMinTypes.put(new VarVersionPair(0, 1), clType);
      mapExprentMaxTypes.put(new VarVersionPair(0, 1), clType);
    }

    int varIndex = 0;
    for (int i = 0; i < md.params.length; i++) {
      mapExprentMinTypes.put(new VarVersionPair(varIndex + (thisVar ? 1 : 0), 1), md.params[i]);
      mapExprentMaxTypes.put(new VarVersionPair(varIndex + (thisVar ? 1 : 0), 1), md.params[i]);
      varIndex += md.params[i].stackSize;
    }

    // catch variables
    LinkedList<Statement> stack = new LinkedList<>();
    stack.add(root);

    while (!stack.isEmpty()) {
      Statement stat = stack.removeFirst();

      List<VarExprent> vars = stat.getImplicitlyDefinedVars();

      if (vars != null) {
        for (VarExprent var : vars) {
          mapExprentMinTypes.put(new VarVersionPair(var.getIndex(), 1), var.getVarType());
          mapExprentMaxTypes.put(new VarVersionPair(var.getIndex(), 1), var.getVarType());
        }
      }

      stack.addAll(stat.getStats());
    }
  }

  private static void resetExprentTypes(DirectGraph graph) {
    graph.iterateExprents(exprent -> {
      List<Exprent> lst = exprent.getAllExprents(true);
      lst.add(exprent);

      for (Exprent expr : lst) {
        if (expr.type == Exprent.EXPRENT_VAR) {
          VarExprent ve = (VarExprent)expr;
          if (ve.getLVT() != null) {
            ve.setVarType(ve.getLVT().getVarType());
          } else {
            ve.setVarType(VarType.VARTYPE_UNKNOWN);
          }
        }
        else if (expr.type == Exprent.EXPRENT_CONST) {
          ConstExprent constExpr = (ConstExprent)expr;
          if (constExpr.getConstType().typeFamily == CodeConstants.TYPE_FAMILY_INTEGER) {
            constExpr.setConstType(new ConstExprent(constExpr.getIntValue(), constExpr.isBoolPermitted(), null).getConstType());
          }
        }
      }
      return 0;
    });
  }

  private boolean processVarTypes(DirectGraph graph) {
    return graph.iterateExprents(exprent -> checkTypeExprent(exprent) ? 0 : 1);
  }

  // true -> Do nothing
  // false -> cancel iteration
  private boolean checkTypeExprent(Exprent exprent) {

    for (Exprent expr : exprent.getAllExprents()) {
      if (!checkTypeExprent(expr)) {
        return false;
      }
    }

    if (exprent.type == Exprent.EXPRENT_CONST) {
      ConstExprent constExpr = (ConstExprent)exprent;
      if (constExpr.getConstType().typeFamily <= CodeConstants.TYPE_FAMILY_INTEGER) { // boolean or integer
        VarVersionPair pair = new VarVersionPair(constExpr.id, -1);
        if (!mapExprentMinTypes.containsKey(pair)) {
          mapExprentMinTypes.put(pair, constExpr.getConstType());
        }
      }
    }

    CheckTypesResult result = exprent.checkExprTypeBounds();

    boolean res = true;
    if (result != null) {
      for (CheckTypesResult.ExprentTypePair entry : result.getLstMaxTypeExprents()) {
        if (entry.type.typeFamily != CodeConstants.TYPE_FAMILY_OBJECT) {
          changeExprentType(entry.exprent, entry.type, 1);
        }
      }

      for (CheckTypesResult.ExprentTypePair entry : result.getLstMinTypeExprents()) {
        res &= changeExprentType(entry.exprent, entry.type, 0);
      }
    }

    return res;
  }


  // true -> Do nothing
  // false -> cancel iteration
  private boolean changeExprentType(Exprent exprent, VarType newType, int minMax) {

    switch (exprent.type) {
      case Exprent.EXPRENT_CONST:
        ConstExprent constExpr = (ConstExprent)exprent;
        VarType constType = constExpr.getConstType();

        if (newType.typeFamily > CodeConstants.TYPE_FAMILY_INTEGER || constType.typeFamily > CodeConstants.TYPE_FAMILY_INTEGER) {
          return true;
        }
        else if (newType.typeFamily == CodeConstants.TYPE_FAMILY_INTEGER) {
          VarType minInteger = new ConstExprent((Integer)constExpr.getValue(), false, null).getConstType();
          if (minInteger.isStrictSuperset(newType)) {
            newType = minInteger;
          }
        }
        return changeVarExprentType(exprent, newType, minMax, new VarVersionPair(exprent.id, -1));
      case Exprent.EXPRENT_VAR:
        return changeVarExprentType(exprent, newType, minMax, new VarVersionPair((VarExprent) exprent));

      case Exprent.EXPRENT_ASSIGNMENT:
        return changeExprentType(((AssignmentExprent)exprent).getRight(), newType, minMax);

      case Exprent.EXPRENT_FUNCTION:
        return changeFunctionExprentType(newType, minMax, (FunctionExprent)exprent);
    }

    return true;
  }

  private boolean changeVarExprentType(Exprent exprent, VarType newType, int minMax, VarVersionPair pair) {
    if (minMax == 0) { // min
      VarType currentMinType = mapExprentMinTypes.get(pair);
      VarType newMinType;
      if (currentMinType == null || newType.typeFamily > currentMinType.typeFamily) {
        newMinType = newType;
      } else if (newType.typeFamily < currentMinType.typeFamily) {
        return true;
      } else {
        newMinType = VarType.getCommonSupertype(currentMinType, newType);
      }

      mapExprentMinTypes.put(pair, newMinType);
      if (exprent.type == Exprent.EXPRENT_CONST) {
        ((ConstExprent) exprent).setConstType(newMinType);
      }

      if (currentMinType != null && (newMinType.typeFamily > currentMinType.typeFamily || newMinType.isStrictSuperset(currentMinType))) {
        return false;
      }
    } else {  // max
      VarType currentMaxType = mapExprentMaxTypes.get(pair);
      VarType newMaxType;
      if (currentMaxType == null || newType.typeFamily < currentMaxType.typeFamily) {
        newMaxType = newType;
      } else if (newType.typeFamily > currentMaxType.typeFamily) {
        return true;
      } else {
        newMaxType = VarType.getCommonMinType(currentMaxType, newType);
      }

      mapExprentMaxTypes.put(pair, newMaxType);
    }
    return true;
  }

  private boolean changeFunctionExprentType(VarType newType, int minMax, FunctionExprent func) {
    int offset = 0;
    switch (func.getFuncType()) {
      case TERNARY:   // FIXME:
        offset++;
      case AND:
      case OR:
      case XOR:
        return changeExprentType(func.getLstOperands().get(offset), newType, minMax) &
               changeExprentType(func.getLstOperands().get(offset + 1), newType, minMax);
    }
    return true;
  }

  public Map<VarVersionPair, VarType> getMapExprentMaxTypes() {
    return mapExprentMaxTypes;
  }

  public Map<VarVersionPair, VarType> getMapExprentMinTypes() {
    return mapExprentMinTypes;
  }

  public Map<VarVersionPair, Integer> getMapFinalVars() {
    return mapFinalVars;
  }

  public void setVarType(VarVersionPair pair, VarType type) {
    mapExprentMinTypes.put(pair, type);
  }

  public VarType getVarType(VarVersionPair pair) {
    return mapExprentMinTypes.get(pair);
  }
}