/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.VarMapHolder;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.CheckTypesResult;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericClassDescriptor;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class AssignmentExprent extends Exprent {
  private Exprent left;
  private Exprent right;
  private FunctionExprent.FunctionType condType = null;

  public AssignmentExprent(Exprent left, Exprent right, BitSet bytecodeOffsets) {
    super(Type.ASSIGNMENT);
    this.left = left;
    this.right = right;

    addBytecodeOffsets(bytecodeOffsets);
  }

  public AssignmentExprent(Exprent left, Exprent right, FunctionExprent.FunctionType condType, BitSet bytecodeOffsets) {
    this(left, right, bytecodeOffsets);
    this.condType = condType;
  }

  @Override
  public VarType getExprType() {
    // Union together types
    VarType rType = VarType.getCommonSupertype(left.getExprType(), right.getExprType());
    // TODO: maybe there's a better default for null
    return rType == null ? left.getExprType() : rType;
  }

  @Override
  public VarType getInferredExprType(VarType upperBounds) {
    return left.getInferredExprType(upperBounds);
  }

  @Override
  public CheckTypesResult checkExprTypeBounds() {
    CheckTypesResult result = new CheckTypesResult();

    VarType typeLeft = left.getExprType();
    VarType typeRight = right.getExprType();

    if (typeLeft.typeFamily.isGreater(typeRight.typeFamily)) {
      result.addMinTypeExprent(right, VarType.getMinTypeInFamily(typeLeft.typeFamily));
    } else if (typeLeft.typeFamily.isLesser(typeRight.typeFamily)) {
      result.addMinTypeExprent(left, typeRight);
    }
    else {
      result.addMinTypeExprent(left, VarType.getCommonSupertype(typeLeft, typeRight));
    }

    return result;
  }

  @Override
  public List<Exprent> getAllExprents(List<Exprent> lst) {
    lst.add(left);
    lst.add(right);
    return lst;
  }

  @Override
  public Exprent copy() {
    return new AssignmentExprent(left.copy(), right.copy(), condType, bytecode);
  }

  @Override
  public int getPrecedence() {
    return 13;
  }

  @Override
  public TextBuffer toJava(int indent) {
    VarType leftType = left.getInferredExprType(null);

    boolean fieldInClassInit = false, hiddenField = false;
    if (left instanceof FieldExprent) { // first assignment to a final field. Field name without "this" in front of it
      FieldExprent field = (FieldExprent) left;
      ClassNode node = ((ClassNode) DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS_NODE));
      if (node != null) {
        StructField fd = node.classStruct.getField(field.getName(), field.getDescriptor().descriptorString);
        if (fd != null) {
          if (field.isStatic() && fd.hasModifier(CodeConstants.ACC_FINAL)) {
            fieldInClassInit = true;
          }
          if (node.getWrapper() != null && node.getWrapper().getHiddenMembers().contains(InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor()))) {
            hiddenField = true;
          }
        }
      }
    }

    if (hiddenField) {
      return new TextBuffer();
    }

    TextBuffer buffer = new TextBuffer();

    if (fieldInClassInit) {
      FieldExprent field = (FieldExprent) left;
      buffer.appendField(field.getName(), false, field.getClassname(), field.getName(), field.getDescriptor());
    } else {
      buffer.append(left.toJava(indent));
    }

    if (right instanceof ConstExprent) {
      ((ConstExprent) right).adjustConstType(leftType);
    }

    this.optimizeCastForAssign();

    if (condType == null) {
      buffer.append(" = ");

      // We must lock the collector: this prevents the retrieval of the cast type name to impact the import list.
      // This is fine as we're only using the cast type name to ensure that it's not the unrepresentable type.
      String castName;
      try (var lock = DecompilerContext.getImportCollector().lock()) {
        castName = ExprProcessor.getCastTypeName(leftType);
      }

      if (castName.equals(ExprProcessor.UNREPRESENTABLE_TYPE_STRING)) {
        // Unrepresentable, go ahead and just put the type on the right. The lhs (if a variable) should know about its type and change itself to "var" accordingly.
        buffer.append(right.toJava(indent));
      } else {
        // Cast with the left type
        ExprProcessor.getCastedExprent(right, leftType, buffer, indent, ExprProcessor.NullCastType.DONT_CAST_AT_ALL, false, false, false);
      }
    } else {
      buffer.append(" ").append(condType.operator).append("= ");
      buffer.append(right.toJava(indent));
    }

    buffer.addStartBytecodeMapping(bytecode);

    if (this.left instanceof VarExprent && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILER_COMMENTS)) {
      VarExprent varLeft = (VarExprent) this.left;

      if (varLeft.isDefinition() && varLeft.getProcessor() != null) {
        if (varLeft.getProcessor().getSyntheticSemaphores().contains(varLeft.getIndex())) {
          buffer.append(" /* VF: Semaphore variable */");
        }
      }
    }

    return buffer;
  }

  // E var = (T)expr; -> E var = (E)expr;
  // when E extends T & A
  private void optimizeCastForAssign() {
    if (!(this.right instanceof FunctionExprent)) {
      return;
    }

    FunctionExprent func = (FunctionExprent) this.right;

    if (func.getFuncType() != FunctionExprent.FunctionType.CAST) {
      return;
    }

    Exprent cast = func.getLstOperands().get(1);

    // Fix for Object[] arr = (Object[])o; where is o is of type Object
    if (!func.doesCast() && this.left instanceof VarExprent) {
      // Same logic as FunctionExprent#getInferredExprType
      if (DecompilerContext.getStructContext().instanceOf(this.right.getExprType().value, cast.getExprType().value)) {
        Exprent castVal = func.getLstOperands().get(0);

        if (this.left.getExprType().arrayDim > castVal.getExprType().arrayDim) {
          func.setNeedsCast(true);
          return;
        }
      }
    }

    VarType leftType = this.left.getInferredExprType(null);

    if (!(leftType instanceof GenericType)) {
      return;
    }

    MethodWrapper method = (MethodWrapper) DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
    if (method == null) {
      return;
    }

    StructMethod mt = method.methodStruct;
    GenericMethodDescriptor descriptor = mt.getSignature();


    if (descriptor == null || descriptor.typeParameters.isEmpty()) {
      return;
    }

    List<String> params = descriptor.typeParameters;
    int index = params.indexOf(leftType.value);
    if (index == -1) {
      return;
    }

    List<List<VarType>> bounds = descriptor.typeParameterBounds;

    List<VarType> types = bounds.get(index);

    GenericClassDescriptor classDescriptor = method.classStruct.getSignature();
    if (classDescriptor != null) {
      for (VarType type : new ArrayList<>(types)) {
        int idex = classDescriptor.fparameters.indexOf(type.value);

        if (idex != -1) {
          types.addAll(classDescriptor.fbounds.get(idex));
        }
      }
    }

    VarType rightType = cast.getInferredExprType(leftType);

    // Check if type bound includes the type that we are attempting to cast to
    boolean didReset = false;
    for (VarType type : types) {
      if (rightType.value.equals(type.value)) {
        ((ConstExprent)cast).setConstType(leftType);
        didReset = true;
      }
    }

    if (didReset) {
      // Reset cast state
      func.getInferredExprType(null);
    }
  }

  @Override
  public void replaceExprent(Exprent oldExpr, Exprent newExpr) {
    if (oldExpr == left) {
      left = newExpr;
    }
    if (oldExpr == right) {
      right = newExpr;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof AssignmentExprent)) return false;

    AssignmentExprent as = (AssignmentExprent)o;
    return InterpreterUtil.equalObjects(left, as.getLeft()) &&
           InterpreterUtil.equalObjects(right, as.getRight()) &&
           condType == as.getCondType();
  }

  @Override
  public void getBytecodeRange(BitSet values) {
    measureBytecode(values, left);
    measureBytecode(values, right);
    measureBytecode(values);
  }

  @Override
  public void processSforms(SFormsConstructor sFormsConstructor, VarMapHolder varMaps, Statement stat, boolean calcLiveVars) {
    Exprent dest = this.left;
    switch (dest.type) {
      case VAR: {
        final VarExprent destVar = (VarExprent) dest;

        if (this.condType != null) {
          destVar.processSforms(sFormsConstructor, varMaps, stat, calcLiveVars);
          this.getRight().processSforms(sFormsConstructor, varMaps, stat, calcLiveVars);

          // make sure we are in normal form (eg `x &= ...`)
          SFormsFastMapDirect varMap = varMaps.toNormal();

          varMap.setCurrentVar(sFormsConstructor.getOrCreatePhantom(destVar.getVarVersionPair()));
        } else {
          this.getRight().processSforms(sFormsConstructor, varMaps, stat, calcLiveVars);
          sFormsConstructor.updateVarExprent(destVar, stat, varMaps.toNormal(), calcLiveVars);

          if (sFormsConstructor.trackDirectAssignments) {
            switch (this.right.type) {
              case VAR: {
                VarVersionPair rightpaar = ((VarExprent) this.right).getVarVersionPair();
                sFormsConstructor.markDirectAssignment(destVar.getVarVersionPair(), rightpaar);
                break;
              }
              case FIELD: {
                int index = sFormsConstructor.getFieldIndex((FieldExprent) this.right);
                VarVersionPair rightpaar = new VarVersionPair(index, 0);
                sFormsConstructor.markDirectAssignment(destVar.getVarVersionPair(), rightpaar);
                break;
              }
            }
          }
        }

        return;
      }
      case FIELD: {
        this.getLeft().processSforms(sFormsConstructor, varMaps, stat, calcLiveVars);
        varMaps.assertIsNormal(); // the left side of an assignment can't be a boolean expression
        this.getRight().processSforms(sFormsConstructor, varMaps, stat, calcLiveVars);
        varMaps.toNormal();
        varMaps.getNormal().removeAllFields();
        // assignment to a field resets all fields. (could be more precise, but this is easier)
        return;
      }
      default: {
        this.getLeft().processSforms(sFormsConstructor, varMaps, stat, calcLiveVars);
        varMaps.assertIsNormal(); // the left side of an assignment can't be a boolean expression
        this.getRight().processSforms(sFormsConstructor, varMaps, stat, calcLiveVars);
        varMaps.toNormal();
        return;
      }
    }
  }

  // *****************************************************************************
  // getter and setter methods
  // *****************************************************************************

  public Exprent getLeft() {
    return left;
  }

  public Exprent getRight() {
    return right;
  }

  public void setRight(Exprent right) {
    this.right = right;
  }

  /**
   * the type of assignment, eg {@code =}, {@code +=}, {@code -=}, etc.
   */
  public FunctionExprent.FunctionType getCondType() {
    return condType;
  }

  public void setCondType(FunctionExprent.FunctionType condType) {
    this.condType = condType;
  }
}
