/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.VarMapHolder;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.CheckTypesResult;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericClassDescriptor;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;
import org.jetbrains.java.decompiler.struct.match.IMatchable;
import org.jetbrains.java.decompiler.struct.match.MatchEngine;
import org.jetbrains.java.decompiler.struct.match.MatchNode;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.*;

public abstract class Exprent implements IMatchable {
  public static final int MULTIPLE_USES = 1;
  public static final int SIDE_EFFECTS_FREE = 2;
  public static final int BOTH_FLAGS = 3;

  public enum Type {
    ANNOTATION,
    ARRAY,
    ASSERT,
    ASSIGNMENT,
    CONST,
    EXIT,
    FIELD,
    FUNCTION,
    IF,
    INVOCATION,
    MONITOR,
    NEW,
    SWITCH,
    SWITCH_HEAD,
    VAR,
    YIELD,

    // Catch all for plugins
    OTHER
  }

  protected static ThreadLocal<Map<String, VarType>> inferredLambdaTypes = ThreadLocal.withInitial(HashMap::new);

  public final Type type;
  public final int id;
  public BitSet bytecode = null;  // offsets of bytecode instructions decompiled to this exprent

  protected Exprent(Type type) {
    this.type = type;
    this.id = DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.EXPRESSION_COUNTER);
  }

  public int getPrecedence() {
    return 0; // the highest precedence
  }

  public VarType getExprType() {
    return VarType.VARTYPE_VOID;
  }

  // TODO: This captures the state of upperBound, find a way to do it without modifying state?
  public VarType getInferredExprType(VarType upperBound) {
    return getExprType();
  }

  public int getExprentUse() {
    return 0;
  }

  public CheckTypesResult checkExprTypeBounds() {
    return null;
  }

  public boolean containsExprent(Exprent exprent) {
    for (Exprent ex : getAllExprents(true, true)) {
      if (ex.equals(exprent)) {
        return true;
      }
    }

    return false;
  }

  public boolean containsVar(VarVersionPair var) {
    if (this instanceof VarExprent) {
      VarExprent varex = (VarExprent)this;
      return varex.getVarVersionPair().equals(var);
    }

    List<Exprent> lst = getAllExprents();
    for (int i = lst.size() - 1; i >= 0; i--) {
      if (lst.get(i).containsVar(var)) {
        return true;
      }
    }

    return false;
  }

  public final List<Exprent> getAllExprents(boolean recursive) {
    return getAllExprents(recursive, false);
  }

  public final List<Exprent> getAllExprents(boolean recursive, boolean self) {
    List<Exprent> lst = new ArrayList<>();
    getAllExprents(recursive, lst);

    if (self) {
      lst.add(this);
    }

    return lst;
  }

  private List<Exprent> getAllExprents(boolean recursive, List<Exprent> list) {
    int start = list.size();
    getAllExprents(list);
    int end = list.size();
    ValidationHelper.assertTrue(start <= end, "inconsistent list size! " + start + " <= " + end);

    if (recursive) {
      for (int i = end - 1; i >= start; i--) {
        list.get(i).getAllExprents(true, list);
      }
    }

    return list;
  }

  public Set<VarVersionPair> getAllVariables() {
    List<Exprent> lstAllExprents = getAllExprents(true);
    lstAllExprents.add(this);

    Set<VarVersionPair> set = new HashSet<>();
    for (Exprent expr : lstAllExprents) {
      if (expr instanceof VarExprent) {
        set.add(new VarVersionPair((VarExprent)expr));
      }
    }
    return set;
  }

  public final List<Exprent> getAllExprents() {
    List<Exprent> list = new ArrayList<>();
    getAllExprents(list);

    return list;
  }

  // Get all the exprents contained within the current one
  // Preconditions: this list must never be removed from! Only added to!
  protected abstract List<Exprent> getAllExprents(List<Exprent> list);

  public abstract Exprent copy();

  public TextBuffer toJava() {
    return toJava(0);
  }

  public abstract TextBuffer toJava(int indent);

  public void replaceExprent(Exprent oldExpr, Exprent newExpr) { }

  public void addBytecodeOffsets(BitSet bytecodeOffsets) {
    if (bytecodeOffsets != null) {
      if (bytecode == null) {
        bytecode = new BitSet();
      }
      bytecode.or(bytecodeOffsets);
    }
  }

  public abstract void getBytecodeRange(BitSet values);

  protected void measureBytecode(BitSet values) {
    if (bytecode != null && values != null) {
      values.or(bytecode);
    }
  }

  protected static void measureBytecode(BitSet values, Exprent exprent) {
    if (exprent != null)
      exprent.getBytecodeRange(values);
  }

  protected static void measureBytecode(BitSet values, List<? extends Exprent> list) {
    if (list != null && !list.isEmpty()) {
      for (Exprent e : list)
        e.getBytecodeRange(values);
    }
  }

  public static List<? extends Exprent> sortIndexed(List<? extends Exprent> lst) {
      List<Exprent> ret = new ArrayList<Exprent>();
      List<VarExprent> defs = new ArrayList<VarExprent>();

      Comparator<VarExprent> comp = new Comparator<VarExprent>() {
        public int compare(VarExprent o1, VarExprent o2) {
          return o1.getIndex() - o2.getIndex();
        }
      };

      for (Exprent exp : lst) {
        boolean isDef = exp instanceof VarExprent && ((VarExprent)exp).isDefinition();
        if (!isDef) {
          if (defs.size() > 0) {
            Collections.sort(defs, comp);
            ret.addAll(defs);
            defs.clear();
          }
          ret.add(exp);
        }
        else {
          defs.add((VarExprent)exp);
        }
      }

      if (defs.size() > 0) {
        Collections.sort(defs, comp);
        ret.addAll(defs);
      }
      return ret;
    }

  protected void gatherGenerics(VarType upperBound, VarType ret, Map<VarType, VarType> genericsMap) {
    // List<T> -> List<String>
    if (upperBound != null && upperBound.isGeneric() && ret.isGeneric() && upperBound.arrayDim == ret.arrayDim) {
      int left = ((GenericType)upperBound).getArguments().size();
      int right = ((GenericType)ret).getArguments().size();
      if (left == right) {
        ((GenericType)ret).mapGenVarsTo((GenericType)upperBound, genericsMap);
      }
    }
  }

  protected void getGenericArgs(List<String> fparams, Map<VarType, VarType> genericsMap, List<VarType> genericArgs) {
    for (String type : fparams) {
      VarType arg = genericsMap.get(GenericType.parse("T" + type + ";"));
      if (arg == null || (arg.isGeneric() && ((GenericType)arg).getWildcard() != GenericType.WILDCARD_NO)) {
        genericArgs.clear();
        break;
      }
      genericArgs.add(arg);
    }
  }

  protected void appendParameters(TextBuffer buf, List<VarType> genericArgs) {
    if (genericArgs.isEmpty()) {
      return;
    }
    buf.append("<");
    //TODO: Check target output level and use <> operator?
    for (int i = 0; i < genericArgs.size(); i++) {
      buf.appendCastTypeName(genericArgs.get(i));
      if(i + 1 < genericArgs.size()) {
        buf.append(", ");
      }
    }
    buf.append(">");
  }

  public Map<VarType, List<VarType>> getNamedGenerics() {
    Map<VarType, List<VarType>> ret = new HashMap<>();
    ClassNode class_ = (ClassNode)DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS_NODE);
    MethodWrapper method = (MethodWrapper)DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);

    while (true) {
      GenericClassDescriptor cls = class_ == null ? null : class_.classStruct.getSignature();
      if (cls != null) {
        for (int x = 0; x < cls.fparameters.size(); x++) {
          ret.put(GenericType.parse("T" + cls.fparameters.get(x) + ";"), cls.fbounds.get(x));
        }
      }

      GenericMethodDescriptor mtd = method == null ? null : method.methodStruct.getSignature();
      if (mtd != null) {
        for (int x = 0; x < mtd.typeParameters.size(); x++) {
          ret.put(GenericType.parse("T" + mtd.typeParameters.get(x) + ";"), mtd.typeParameterBounds.get(x));
        }
      }

      if (class_ == null || class_.parent == null) {
        break;
      }
      method = class_.enclosingMethod == null ? null : class_.parent.getWrapper().getMethods().getWithKey(class_.enclosingMethod);
      class_ = class_.parent;
    }
    return ret;
  }

  public void setInvocationInstance() {}

  public void setIsQualifier() {}

  public boolean allowNewlineAfterQualifier() {
    return true;
  }

  // processes exprents, much like section 16.1. of the java language specifications
  // (Definite Assignment and Expressions).
  public void processSforms(SFormsConstructor sFormsConstructor, VarMapHolder varMaps, Statement stat, boolean calcLiveVars) {

    for (Exprent ex : this.getAllExprents()) {
      ex.processSforms(sFormsConstructor, varMaps, stat, calcLiveVars);
      varMaps.toNormal();
    }
  }

  // *****************************************************************************
  // IMatchable implementation
  // *****************************************************************************

  @Override
  public IMatchable findObject(MatchNode matchNode, int index) {
    if (matchNode.getType() != MatchNode.MATCHNODE_EXPRENT) {
      return null;
    }

    List<Exprent> lstAllExprents = getAllExprents();
    if (lstAllExprents == null || lstAllExprents.isEmpty()) {
      return null;
    }

    String position = (String)matchNode.getRuleValue(MatchProperties.EXPRENT_POSITION);
    if (position != null) {
      if (position.matches("-?\\d+")) {
        return lstAllExprents.get((lstAllExprents.size() + Integer.parseInt(position)) % lstAllExprents.size()); // care for negative positions
      }
    }
    else if (index < lstAllExprents.size()) { // use 'index' parameter
      return lstAllExprents.get(index);
    }

    return null;
  }

  @Override
  public boolean match(MatchNode matchNode, MatchEngine engine) {
    if (matchNode.getType() != MatchNode.MATCHNODE_EXPRENT) {
      return false;
    }

    return matchNode.iterateRules((key, value) -> {
      if (key == MatchProperties.EXPRENT_TYPE && this.type != value.value) {
        return false;
      }

      return key != MatchProperties.EXPRENT_RET || engine.checkAndSetVariableValue((String) value.value, this);
    });
  }

  @Override
  public String toString() {
    return toJava(0).convertToStringAndAllowDataDiscard();
  }
}