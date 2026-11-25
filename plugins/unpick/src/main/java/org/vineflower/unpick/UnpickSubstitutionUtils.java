package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.TypeAnnotation;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructConstantValueAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.MutableObject;
import org.jetbrains.java.decompiler.util.Pair;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.GroupFormat;
import org.vineflower.unpick.parser.tree.Literal;
import org.vineflower.unpick.parser.tree.TargetAnnotation;
import org.vineflower.unpick.parser.tree.TargetField;
import org.vineflower.unpick.parser.tree.expr.BinaryExpression;
import org.vineflower.unpick.parser.tree.expr.Expression;
import org.vineflower.unpick.parser.tree.expr.ExpressionVisitor;
import org.vineflower.unpick.parser.tree.expr.FieldExpression;
import org.vineflower.unpick.parser.tree.expr.LiteralExpression;
import org.vineflower.unpick.parser.tree.expr.UnaryExpression;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class UnpickSubstitutionUtils {
  private UnpickSubstitutionUtils() {
  }

  static boolean substituteConstants(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    Map<Pair<String, Integer>, String> exprentGroups
  ) {
    boolean res = false;

    for (MethodWrapper method : classNode.getWrapper().getMethods()) {
      if (method.root != null) {
        res |= substituteConstants(defs, method, method.root, exprentGroups);
      }
    }
    for (ClassesProcessor.ClassNode nested : classNode.nested) {
      res |= substituteConstants(defs, nested, exprentGroups);
    }

    return res;
  }

  private static boolean substituteConstants(
    UnpickDefinitions defs,
    MethodWrapper method,
    Statement stat,
    Map<Pair<String, Integer>, String> exprentGroups
  ) {
    boolean res = false;

    if (stat.getExprents() == null) {
      for (Exprent exprent : stat.getStatExprents()) {
        res |= substituteConstants(defs, method, Set.of(), null, exprent, newExprent -> stat.replaceExprent(exprent, newExprent), exprentGroups);
      }

      for (Statement st : stat.getStats()) {
        res |= substituteConstants(defs, method, st, exprentGroups);
      }
    } else {
      for (Exprent exprent : stat.getExprents()) {
        res |= substituteConstants(defs, method, Set.of(), null, exprent, newExprent -> stat.replaceExprent(exprent, newExprent), exprentGroups);
      }
    }

    return res;
  }

  // TODO: handle casting to the right type when overload resolution is affected
  private static boolean substituteConstants(
    UnpickDefinitions defs,
    MethodWrapper method,
    Set<String> disallowedFieldRefs,
    @Nullable StructField fieldAssignment,
    Exprent exprent,
    Consumer<Exprent> replace,
    Map<Pair<String, Integer>, String> exprentGroups
  ) {
    boolean res = false;

    if (exprent instanceof AssignmentExprent assignmentExprent && assignmentExprent.getLeft() instanceof FieldExprent fieldExprent) {
      if (fieldExprent.getClassname().equals(method.classStruct.qualifiedName)) {
        StructField fd = method.classStruct.getField(fieldExprent.getName(), fieldExprent.getDescriptor().descriptorString);
        if (fd != null && fd.hasModifier(CodeConstants.ACC_FINAL)) {
          disallowedFieldRefs = method.classStruct.getFields().stream()
            .dropWhile(f -> f != fd)
            .map(StructField::getName)
            .collect(Collectors.toSet());
          fieldAssignment = fd;
        }
      }
    }

    for (Exprent subExprent : exprent.getAllExprents()) {
      res |= substituteConstants(defs, method, disallowedFieldRefs, fieldAssignment, subExprent, newExprent -> exprent.replaceExprent(subExprent, newExprent), exprentGroups);
    }

    Object constantValue = computeConstantValue(exprent);
    if (constantValue == null) {
      return res;
    }

    String groupName = exprentGroups.get(Pair.of(InterpreterUtil.makeUniqueKey(method.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor()), exprent.id));

    SubstitutionResult substitution = getBestSubstitution(
      defs,
      method.classStruct.qualifiedName,
      method.methodStruct.getName(),
      method.methodStruct.getDescriptor(),
      method.methodStruct.hasModifier(CodeConstants.ACC_STATIC),
      disallowedFieldRefs,
      groupName,
      UnpickUtils.getExprType(exprent),
      constantValue
    );

    if (substitution instanceof SubstitutionResult.Expr expr) {
      if (fieldAssignment != null && detectInterClassFieldReferenceCycle(defs, method.classStruct, fieldAssignment, expr.expr(), new HashSet<>())) {
        return res;
      }

      ExpressionToExprent converter = new ExpressionToExprent(method.methodStruct.hasModifier(CodeConstants.ACC_STATIC) ? null : method, exprent.bytecode);
      expr.expr().accept(converter);
      replace.accept(converter.result);
      return true;
    } else if (substitution instanceof SubstitutionResult.Format format) {
      if (exprent instanceof ConstExprent constExprent) {
        res |= applyFormatToConst(constExprent, format.format());
      }

      return res;
    } else {
      return res;
    }
  }

  // Constants in the ConstantValue attribute are not stored in the exprent tree, so we have to handle them separately
  static boolean substituteConstantFieldInitializers(ClassesProcessor.ClassNode classNode, UnpickDefinitions defs) {
    boolean res = false;

    for (StructField fd : classNode.classStruct.getFields()) {
      SubstitutionResult substitution = getFieldValueSubstitution(defs, classNode.classStruct, fd);
      if (substitution instanceof SubstitutionResult.Expr expr) {
        if (!detectInterClassFieldReferenceCycle(defs, classNode.classStruct, fd, expr.expr(), new HashSet<>())) {
          if (fd.hasModifier(CodeConstants.ACC_STATIC)) {
            ExpressionToExprent converter = new ExpressionToExprent(null, null);
            expr.expr().accept(converter);
            classNode.getWrapper().getStaticFieldInitializers().addWithKey(converter.result, InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor()));
            res = true;
          } else {
            MethodWrapper constructor = null;
            for (MethodWrapper method : classNode.getWrapper().getMethods()) {
              if (method.methodStruct.getName().equals("<init>")) {
                constructor = method;
                break;
              }
            }

            if (constructor != null) {
              ExpressionToExprent converter = new ExpressionToExprent(constructor, null);
              expr.expr().accept(converter);
              classNode.getWrapper().getDynamicFieldInitializers().addWithKey(converter.result, InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor()));
              res = true;
            }
          }
        }
      } else if (substitution instanceof SubstitutionResult.Format format) {
        Object constantValue = classNode.classStruct.getPool().getPrimitiveConstant(fd.getAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE).getIndex()).value;
        ConstExprent constExprent = new ConstExprent(new VarType(fd.getDescriptor()), constantValue, null);
        if (applyFormatToConst(constExprent, format.format())) {
          if (fd.hasModifier(CodeConstants.ACC_STATIC)) {
            classNode.getWrapper().getStaticFieldInitializers().addWithKey(constExprent, InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor()));
          } else {
            classNode.getWrapper().getDynamicFieldInitializers().addWithKey(constExprent, InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor()));
          }
          res = true;
        }
      }
    }

    return res;
  }

  private static boolean applyFormatToConst(ConstExprent constExprent, GroupFormat format) {
    boolean res = false;

    if (format == GroupFormat.CHAR) {
      if (DataTypeUtils.fromVarType(constExprent.getConstType()) == DataType.INT && !constExprent.getConstType().equals(VarType.VARTYPE_CHAR)) {
        constExprent.setConstType(VarType.VARTYPE_CHAR);
        res = true;
      }
    } else {
      int radix = switch (format) {
        case BINARY -> 2;
        case OCTAL -> 8;
        case DECIMAL -> 10;
        case HEX -> 16;
        default -> throw new IllegalStateException("Unknown group format");
      };
      if (constExprent.getConstType().equals(VarType.VARTYPE_CHAR)) {
        constExprent.setConstType(VarType.VARTYPE_INT);
        res = true;
      }
      if (constExprent.getRadix() != radix) {
        constExprent.setRadix(radix);
        res = true;
      }
    }

    return res;
  }

  @Nullable
  private static SubstitutionResult getBestSubstitution(
    UnpickDefinitions defs,
    String containingClass,
    String containingMethodName,
    String containingMethodDesc,
    boolean isStatic,
    Set<String> disallowedFieldRefs,
    @Nullable String groupName,
    VarType type,
    Object constantValue
  ) {
    DataType constantType = DataTypeUtils.getDataType(constantValue);
    GroupFormat bestFormat = null;
    for (UnpickDefinitions.GroupData groupData : getGroupData(defs, groupName, type)) {
      Long targetFlags;
      if (groupData.isFlags && (targetFlags = (Long) DataTypeUtils.tryCastExact(constantValue, DataType.LONG)) != null && targetFlags != 0 && targetFlags != -1) {
        return getBestFlagsSubstitution(containingClass, containingMethodName, containingMethodDesc, isStatic, disallowedFieldRefs, groupData, constantType, targetFlags);
      }

      for (UnpickDefinitions.ScopedData scopedData : getScopedData(groupData, containingClass, containingMethodName, containingMethodDesc)) {
        if (bestFormat == null) {
          bestFormat = scopedData.format;
        }

        UnpickDefinitions.ConstantReplacementData replacementData = scopedData.constantReplacementMap.get(DataTypeUtils.tryCastExact(constantValue, groupData.dataType));
        if (replacementData != null && !referencesDisallowedField(replacementData.replacementExpr(), containingClass, disallowedFieldRefs)) {
          if (!isStatic || !referencesInstanceField(replacementData.replacementExpr())) {
            boolean compatibleTypes = replacementData.strict() ? groupData.dataType == constantType : DataTypeUtils.isAssignable(constantType, groupData.dataType);
            if (compatibleTypes) {
              return new SubstitutionResult.Expr(replacementData.replacementExpr());
            }
          }
        }
      }
    }

    return bestFormat == null ? null : new SubstitutionResult.Format(bestFormat);
  }

  @Nullable
  private static SubstitutionResult getBestFlagsSubstitution(
    String containingClass,
    String containingMethodName,
    String containingMethodDesc,
    boolean isStatic,
    Set<String> disallowedFieldRefs,
    UnpickDefinitions.GroupData groupData,
    DataType constantType,
    long targetFlags
  ) {
    long flagsMask = constantType == DataType.LONG ? 0xffff_ffff_ffff_ffffL : 0xffff_ffffL;
    targetFlags &= flagsMask;

    GroupFormat bestFormat = null;
    Map<Long, Expression> replacementData = new LinkedHashMap<>();
    for (UnpickDefinitions.ScopedData scopedData : getScopedData(groupData, containingClass, containingMethodName, containingMethodDesc)) {
      if (bestFormat == null) {
        bestFormat = scopedData.format;
      }

      scopedData.constantReplacementMap.forEach((value, data) -> {
        Long flag = (Long) DataTypeUtils.tryCastExact(value, DataType.LONG);
        if (flag != null) {
          if (!referencesDisallowedField(data.replacementExpr(), containingClass, disallowedFieldRefs)) {
            if (!isStatic || !referencesInstanceField(data.replacementExpr())) {
              if (!data.strict() || groupData.dataType == constantType) {
                flag &= flagsMask;
                replacementData.putIfAbsent(flag, data.replacementExpr());
              }
            }
          }
        }
      });
    }

    List<Expression> positiveSet = new ArrayList<>();
    long positiveResidual = getMinimalFlags(targetFlags, replacementData, positiveSet);
    List<Expression> negativeSet = new ArrayList<>();
    long negativeResidual = getMinimalFlags(~targetFlags & flagsMask, replacementData, negativeSet);

    if (negativeResidual == 0 && (positiveResidual != 0 || negativeSet.size() < positiveSet.size())) {
      Expression resultExpression = negativeSet.get(0);
      for (int i = 1; i < negativeSet.size(); i++) {
        resultExpression = new BinaryExpression(resultExpression, negativeSet.get(i), BinaryExpression.Operator.BIT_OR);
      }
      resultExpression = new UnaryExpression(resultExpression, UnaryExpression.Operator.BIT_NOT);
      return new SubstitutionResult.Expr(resultExpression);
    } else {
      if (positiveSet.isEmpty()) {
        return bestFormat == null ? null : new SubstitutionResult.Format(bestFormat);
      }

      Expression resultExpression = positiveSet.get(0);
      for (int i = 1; i < positiveSet.size(); i++) {
        resultExpression = new BinaryExpression(resultExpression, positiveSet.get(i), BinaryExpression.Operator.BIT_OR);
      }

      if (positiveResidual != 0) {
        Expression residualExpr;
        if (Long.compareUnsigned(positiveResidual, Character.MAX_VALUE) < 0 && bestFormat == GroupFormat.CHAR) {
          residualExpr = new LiteralExpression(new Literal.Character((char) positiveResidual));
        } else {
          int radix;
          if (bestFormat == null) {
            radix = 10;
          } else {
            radix = switch (bestFormat) {
              case BINARY -> 2;
              case OCTAL -> 8;
              case HEX -> 16;
              default -> 10;
            };
          }
          if (constantType == DataType.LONG) {
            residualExpr = new LiteralExpression(new Literal.Long(positiveResidual, radix));
          } else {
            residualExpr = new LiteralExpression(new Literal.Integer((int) positiveResidual, radix));
          }
        }
        resultExpression = new BinaryExpression(resultExpression, residualExpr, BinaryExpression.Operator.BIT_OR);
      }

      return new SubstitutionResult.Expr(resultExpression);
    }
  }

  // outputs to flags, returns any unset bits
  private static long getMinimalFlags(long targetValue, Map<Long, Expression> replacementData, List<Expression> flags) {
    List<Pair<Long, Expression>> candidates = new ArrayList<>(replacementData.size());
    replacementData.forEach((flag, expr) -> {
      // remove flags that set bits that are not in the target value
      if ((flag & ~targetValue) == 0) {
        candidates.add(Pair.of(flag, expr));
      }
    });
    // sort into reverse order of bit count, so that flags that set the most bits are added first
    candidates.sort(Comparator.comparing(candidate -> -Long.bitCount(candidate.a)));

    long residual = targetValue;
    for (Pair<Long, Expression> candidate : candidates) {
      // check that the candidate contributes any bits to the remaining bits that need setting
      if ((candidate.a & residual) != 0) {
        flags.add(candidate.b);
        // unset the bits in the residual that are set by this flag
        residual &= ~candidate.a;
      }
    }

    return residual;
  }

  @Nullable
  private static SubstitutionResult getFieldValueSubstitution(UnpickDefinitions defs, StructClass cl, StructField fd) {
    StructConstantValueAttribute constantValueAttr = fd.getAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE);
    if (constantValueAttr == null) {
      return null;
    }

    PrimitiveConstant primitiveConstant = cl.getPool().getPrimitiveConstant(constantValueAttr.getIndex());
    Object constantValue = primitiveConstant.type == CodeConstants.CONSTANT_Class ? new ClassLiteral(primitiveConstant.getString()) : primitiveConstant.value;
    boolean isStatic = fd.hasModifier(CodeConstants.ACC_STATIC);

    String containingMethodName = isStatic ? "<clinit>" : "<init>";
    String containingMethodDesc = "()V";
    if (!isStatic) {
      for (StructMethod method : cl.getMethods()) {
        if (method.getName().equals("<init>")) {
          containingMethodDesc = method.getDescriptor();
          break;
        }
      }
    }

    String groupName = null;
    TargetField targetField = defs.targetFields.get(InterpreterUtil.makeUniqueKey(cl.qualifiedName, fd.getName(), fd.getDescriptor()));
    if (targetField != null) {
      groupName = targetField.groupName();
    } else {
      for (AnnotationExprent ann : UnpickUtils.getMemberAnnotations(fd, TypeAnnotation.FIELD)) {
        TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
        if (targetAnnotation != null) {
          groupName = targetAnnotation.groupName();
          break;
        }
      }
    }

    Set<String> disallowedFieldRefs = cl.getFields().stream()
      .dropWhile(f -> f != fd)
      .map(StructField::getName)
      .collect(Collectors.toSet());

    return getBestSubstitution(
      defs,
      cl.qualifiedName,
      containingMethodName,
      containingMethodDesc,
      isStatic,
      disallowedFieldRefs,
      groupName,
      new VarType(fd.getDescriptor()),
      constantValue
    );
  }

  private static boolean detectInterClassFieldReferenceCycle(
    UnpickDefinitions defs,
    StructClass cl,
    StructField fd,
    Expression expression,
    Set<String> seenFields
  ) {
    String fieldKey = InterpreterUtil.makeUniqueKey(cl.qualifiedName, fd.getName(), fd.getDescriptor());
    if (!seenFields.add(fieldKey)) {
      return true;
    }

    MutableObject<Boolean> result = MutableObject.of(false);
    expression.accept(new ExpressionVisitor() {
      @Override
      public void visitFieldExpression(FieldExpression fieldExpression) {
        if (result.get()) {
          // we already know the result, stop checking
          return;
        }

        StructClass resolvedClass = DecompilerContext.getStructContext().getClass(fieldExpression.className.replace('.', '/'));
        StructField resolvedField = null;
        resolveLoop: while (resolvedClass != null) {
          for (StructField field : resolvedClass.getFields()) {
            if (field.getName().equals(fieldExpression.fieldName)) {
              resolvedField = field;
              break resolveLoop;
            }
          }
          resolvedClass = resolvedClass.superClass == null ? null : DecompilerContext.getStructContext().getClass(resolvedClass.superClass.getString());
        }

        if (resolvedField != null) {
          if (getFieldValueSubstitution(defs, resolvedClass, resolvedField) instanceof SubstitutionResult.Expr expr) {
            if (detectInterClassFieldReferenceCycle(defs, resolvedClass, resolvedField, expr.expr(), seenFields)) {
              result.set(true);
            }
          }
        }
      }
    });

    seenFields.remove(fieldKey);
    return result.get();
  }

  private static boolean referencesDisallowedField(Expression expression, String className, Set<String> unreferenceableFields) {
    MutableObject<Boolean> result = MutableObject.of(false);
    expression.accept(new ExpressionVisitor() {
      @Override
      public void visitFieldExpression(FieldExpression fieldExpression) {
        if (fieldExpression.className.replace('.', '/').equals(className) && unreferenceableFields.contains(fieldExpression.fieldName)) {
          result.set(true);
        }
      }
    });
    return result.get();
  }

  private static boolean referencesInstanceField(Expression expression) {
    MutableObject<Boolean> result = MutableObject.of(false);
    expression.accept(new ExpressionVisitor() {
      @Override
      public void visitFieldExpression(FieldExpression fieldExpression) {
        if (!fieldExpression.isStatic) {
          result.set(true);
        }
      }
    });
    return result.get();
  }

  private static List<UnpickDefinitions.GroupData> getGroupData(UnpickDefinitions defs, @Nullable String groupName, VarType varType) {
    if (groupName != null) {
      UnpickDefinitions.GroupData group = defs.groups.get(groupName);
      return group == null ? List.of() : List.of(group);
    }

    DataType dataType = DataTypeUtils.fromVarType(varType);
    if (dataType == null) {
      return List.of();
    }
    List<DataType> narrowedTypes = switch (dataType) {
      case LONG -> List.of(DataType.LONG, DataType.INT);
      case FLOAT -> List.of(DataType.FLOAT, DataType.LONG, DataType.INT);
      case DOUBLE -> List.of(DataType.DOUBLE, DataType.FLOAT, DataType.LONG, DataType.INT);
      case CHAR -> List.of(DataType.INT);
      default -> List.of(dataType);
    };

    List<UnpickDefinitions.GroupData> result = new ArrayList<>(narrowedTypes.size());
    for (DataType narrowedType : narrowedTypes) {
      UnpickDefinitions.GroupData defaultGroup = defs.defaultGroups.get(narrowedType);
      if (defaultGroup != null) {
        result.add(defaultGroup);
      }
    }
    return result;
  }

  // TODO: handle lambdas and anonymous classes
  private static List<UnpickDefinitions.ScopedData> getScopedData(UnpickDefinitions.GroupData groupData, String owner, String name, String desc) {
    List<UnpickDefinitions.ScopedData> result = new ArrayList<>(4);

    UnpickDefinitions.ScopedData methodData = groupData.methodScopes.get(InterpreterUtil.makeUniqueKey(owner, name, desc));
    if (methodData != null) {
      result.add(methodData);
    }

    UnpickDefinitions.ScopedData classData = groupData.classScopes.get(owner);
    if (classData != null) {
      result.add(classData);
    }

    if (owner.contains("/")) {
      UnpickDefinitions.ScopedData packageData = groupData.packageScopes.get(owner.substring(0, owner.lastIndexOf('/')));
      if (packageData != null) {
        result.add(packageData);
      }
    }

    result.add(groupData.globalScope);
    return result;
  }

  // TODO: other types of exprent listed in the spec, including unary minus
  @Nullable
  private static Object computeConstantValue(Exprent exprent) {
    if (exprent instanceof ConstExprent constExprent) {
      if (constExprent.getConstType().equals(VarType.VARTYPE_CLASS)) {
        return new ClassLiteral(constExprent.getValue().toString());
      } else {
        return constExprent.getValue();
      }
    }

    return null;
  }

  private sealed interface SubstitutionResult {
    record Expr(Expression expr) implements SubstitutionResult {
    }

    record Format(GroupFormat format) implements SubstitutionResult {
    }
  }
}
