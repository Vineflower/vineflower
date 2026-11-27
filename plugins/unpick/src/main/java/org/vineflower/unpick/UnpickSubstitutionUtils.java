package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassWriter;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.TypeAnnotation;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMember;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructAnnDefaultAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationParameterAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructConstantValueAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructEnclosingMethodAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructTypeAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.Key;
import org.jetbrains.java.decompiler.util.MutableObject;
import org.jetbrains.java.decompiler.util.Pair;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.GroupFormat;
import org.vineflower.unpick.parser.tree.GroupScope;
import org.vineflower.unpick.parser.tree.Literal;
import org.vineflower.unpick.parser.tree.expr.BinaryExpression;
import org.vineflower.unpick.parser.tree.expr.Expression;
import org.vineflower.unpick.parser.tree.expr.ExpressionVisitor;
import org.vineflower.unpick.parser.tree.expr.FieldExpression;
import org.vineflower.unpick.parser.tree.expr.LiteralExpression;
import org.vineflower.unpick.parser.tree.expr.UnaryExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class UnpickSubstitutionUtils {
  private UnpickSubstitutionUtils() {
  }

  static boolean substituteConstants(UnpickDefinitions defs, ClassesProcessor.ClassNode classNode, GroupTracker groupTracker) {
    MutableObject<Boolean> res = MutableObject.of(false);

    int slashIndex = classNode.classStruct.qualifiedName.lastIndexOf('/');
    String packageName = slashIndex >= 0 ? classNode.classStruct.qualifiedName.substring(0, slashIndex) : null;

    UnpickUtils.iterateAllMethods(classNode, (node, method, classStack) -> {
      List<GroupScope> scopeStack = new ArrayList<>(4);
      if (packageName != null) {
        scopeStack.add(new GroupScope.Package(packageName));
      }
      String lastClassName = null;
      for (ClassesProcessor.ClassNode c : classStack) {
        if (lastClassName != null && c.enclosingMethod != null) {
          String[] nameAndDesc = c.enclosingMethod.split(" ", 2);
          scopeStack.add(new GroupScope.Method(lastClassName, nameAndDesc[0], nameAndDesc[1]));
        }
        if (!c.classStruct.qualifiedName.equals(lastClassName)) {
          lastClassName = c.classStruct.qualifiedName;
          scopeStack.add(new GroupScope.Class(c.classStruct.qualifiedName));
        }
      }
      scopeStack.add(new GroupScope.Method(node.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor()));

      if (method.root != null && substituteConstants(defs, method, scopeStack, method.root, groupTracker)) {
        res.set(true);
      }
    });
    return res.get();
  }

  private static boolean substituteConstants(
    UnpickDefinitions defs,
    MethodWrapper method,
    List<GroupScope> scopeStack,
    Statement stat,
    GroupTracker groupTracker
  ) {
    boolean res = false;

    if (stat.getExprents() == null) {
      for (Exprent exprent : stat.getStatExprents()) {
        res |= substituteConstants(defs, method, scopeStack, Set.of(), null, exprent, newExprent -> stat.replaceExprent(exprent, newExprent), groupTracker);
      }

      for (Statement st : stat.getStats()) {
        res |= substituteConstants(defs, method, scopeStack, st, groupTracker);
      }
    } else {
      for (Exprent exprent : stat.getExprents()) {
        res |= substituteConstants(defs, method, scopeStack, Set.of(), null, exprent, newExprent -> stat.replaceExprent(exprent, newExprent), groupTracker);
      }
    }

    return res;
  }

  private static boolean substituteConstants(
    UnpickDefinitions defs,
    MethodWrapper method,
    List<GroupScope> scopeStack,
    Set<String> disallowedFieldRefs,
    @Nullable StructField fieldAssignment,
    Exprent exprent,
    Consumer<Exprent> replace,
    GroupTracker groupTracker
  ) {
    if (substituteConstantsForSingleExprent(defs, method, scopeStack, disallowedFieldRefs, fieldAssignment, exprent, replace, groupTracker)) {
      return true;
    }

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

    boolean res = false;
    for (Exprent subExprent : exprent.getAllExprents()) {
      res |= substituteConstants(defs, method, scopeStack, disallowedFieldRefs, fieldAssignment, subExprent, newExprent -> exprent.replaceExprent(subExprent, newExprent), groupTracker);
    }
    return res;
  }

  private static boolean substituteConstantsForSingleExprent(
    UnpickDefinitions defs,
    MethodWrapper method,
    List<GroupScope> scopeStack,
    Set<String> disallowedFieldRefs,
    @Nullable StructField fieldAssignment,
    Exprent exprent,
    Consumer<Exprent> replace,
    GroupTracker groupTracker
  ) {
    Object constantValue = computeConstantValue(exprent);
    if (constantValue == null) {
      return false;
    }

    Set<String> groupNames = groupTracker.getExprentGroups(InterpreterUtil.makeUniqueKey(method.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor()), exprent);

    SubstitutionResult substitution = getBestSubstitution(
      defs,
      method.classStruct.qualifiedName,
      scopeStack,
      method.methodStruct.hasModifier(CodeConstants.ACC_STATIC),
      disallowedFieldRefs,
      groupNames,
      UnpickUtils.getExprType(exprent),
      constantValue
    );

    if (substitution instanceof SubstitutionResult.Expr expr) {
      if (fieldAssignment != null && detectInterClassFieldReferenceCycle(defs, method.classStruct, fieldAssignment, expr.expr(), new HashSet<>())) {
        return false;
      }

      ExpressionToExprent converter = new ExpressionToExprent(method.methodStruct.hasModifier(CodeConstants.ACC_STATIC) ? null : method, exprent.bytecode);
      expr.expr().accept(converter);
      replace.accept(converter.result);
      return true;
    } else if (substitution instanceof SubstitutionResult.Format format) {
      return applyFormatToConst(exprent, format.format());
    } else {
      return false;
    }
  }

  // Constants in the ConstantValue attribute are not stored in the exprent tree, so we have to handle them separately
  static boolean substituteConstantFieldInitializers(UnpickDefinitions defs, ClassesProcessor.ClassNode classNode) {
    boolean res = false;

    for (ClassesProcessor.ClassNode nested : classNode.nested) {
      if (nested.type != ClassesProcessor.ClassNode.Type.LAMBDA) {
        res |= substituteConstantFieldInitializers(defs, nested);
      }
    }

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

  static boolean substituteAnnotations(UnpickDefinitions defs, ClassesProcessor.ClassNode classNode) {
    List<GroupScope> scopeStack = new ArrayList<>();
    if (classNode.classStruct.qualifiedName.contains("/")) {
      scopeStack.add(new GroupScope.Package(classNode.classStruct.qualifiedName.substring(0, classNode.classStruct.qualifiedName.lastIndexOf('/'))));
    }
    return substituteAnnotations(defs, classNode, scopeStack);
  }

  private static boolean substituteAnnotations(UnpickDefinitions defs, ClassesProcessor.ClassNode classNode, List<GroupScope> scopeStack) {
    boolean res = false;
    scopeStack.add(new GroupScope.Class(classNode.classStruct.qualifiedName));

    for (ClassesProcessor.ClassNode nested : classNode.nested) {
      if (nested.type != ClassesProcessor.ClassNode.Type.LAMBDA) {
        res |= substituteAnnotations(defs, nested, scopeStack);
      }
    }

    res |= substituteAnnotations(defs, classNode.classStruct, classNode.classStruct.qualifiedName, scopeStack, Set.of());

    Set<String> disallowedFields = classNode.classStruct.getFields().stream().map(StructField::getName).collect(Collectors.toCollection(HashSet::new));
    for (StructField field : classNode.classStruct.getFields()) {
      res |= substituteAnnotations(defs, field, classNode.classStruct.qualifiedName, scopeStack, disallowedFields);
      disallowedFields.remove(field.getName());
    }

    for (StructMethod method : classNode.classStruct.getMethods()) {
      res |= substituteAnnotations(defs, method, classNode.classStruct.qualifiedName, scopeStack, Set.of());

      for (Key<?> attributeKey : ClassWriter.PARAMETER_ANNOTATION_ATTRIBUTES) {
        StructAnnotationParameterAttribute attribute = (StructAnnotationParameterAttribute) method.getAttribute(attributeKey);
        if (attribute != null) {
          for (List<AnnotationExprent> annotationsForParam : attribute.getParamAnnotations()) {
            for (AnnotationExprent ann : annotationsForParam) {
              res |= substituteExprentsInAnnotation(defs, ann, classNode.classStruct.qualifiedName, scopeStack, Set.of());
            }
          }
        }
      }

      res |= substituteAnnotationDefault(defs, method, classNode.classStruct.qualifiedName, scopeStack);
    }

    scopeStack.remove(scopeStack.size() - 1);
    return res;
  }

  private static boolean substituteAnnotations(UnpickDefinitions defs, StructMember member, String containingClass, List<GroupScope> scopeStack, Set<String> disallowedFields) {
    boolean res = false;

    for (Key<?> attributeKey : ClassWriter.ANNOTATION_ATTRIBUTES) {
      StructAnnotationAttribute attribute = (StructAnnotationAttribute) member.getAttribute(attributeKey);
      if (attribute != null) {
        for (AnnotationExprent ann : attribute.getAnnotations()) {
          res |= substituteExprentsInAnnotation(defs, ann, containingClass, scopeStack, disallowedFields);
        }
      }
    }

    for (Key<?> attributeKey : ClassWriter.TYPE_ANNOTATION_ATTRIBUTES) {
      StructTypeAnnotationAttribute attribute = (StructTypeAnnotationAttribute) member.getAttribute(attributeKey);
      if (attribute != null) {
        for (TypeAnnotation typeAnnotation : attribute.getAnnotations()) {
          res |= substituteExprentsInAnnotation(defs, typeAnnotation.getAnnotation(), containingClass, scopeStack, disallowedFields);
        }
      }
    }

    return res;
  }

  private static boolean substituteExprentsInAnnotation(UnpickDefinitions defs, Exprent exprent, String containingClass, List<GroupScope> scopeStack, Set<String> disallowedFields) {
    boolean res = false;
    for (Exprent subExprent : exprent.getAllExprents()) {
      res |= substituteExprentsInAnnotation(defs, subExprent, containingClass, scopeStack, disallowedFields);
    }

    if (exprent instanceof AnnotationExprent annotationExprent) {
      StructClass annotationClass = DecompilerContext.getStructContext().getClass(annotationExprent.getClassName());
      if (annotationClass != null) {
        for (int i = 0; i < annotationExprent.getParNames().size(); i++) {
          String parName = annotationExprent.getParNames().get(i);
          Exprent subExprent = annotationExprent.getParValues().get(i);
          StructMethod mt = annotationClass.getMethods().stream().filter(md -> md.getName().equals(parName)).findFirst().orElse(null);
          if (mt != null) {
            Set<String> groupNames = UnpickTargetUtils.getReturnGroups(defs, mt);
            SubstitutionResult substitution = getBestSubstitution(
              defs,
              containingClass,
              scopeStack,
              true,
              disallowedFields,
              groupNames,
              mt.methodDescriptor().ret,
              computeConstantValue(subExprent)
            );
            if (substitution instanceof SubstitutionResult.Expr expr) {
              ExpressionToExprent converter = new ExpressionToExprent(null, null);
              expr.expr().accept(converter);
              annotationExprent.replaceExprent(subExprent, converter.result);
              res = true;
            } else if (substitution instanceof SubstitutionResult.Format format) {
              res |= applyFormatToConst(subExprent, format.format());
            }
          }
        }
      }
    }

    return res;
  }

  private static boolean substituteAnnotationDefault(UnpickDefinitions defs, StructMethod mt, String containingClass, List<GroupScope> scopeStack) {
    StructAnnDefaultAttribute attribute = mt.getAttribute(StructGeneralAttribute.ATTRIBUTE_ANNOTATION_DEFAULT);
    if (attribute == null) {
      return false;
    }

    if (attribute.getDefaultValue() instanceof ConstExprent constExprent) {
      Set<String> groupNames = UnpickTargetUtils.getReturnGroups(defs, mt);
      SubstitutionResult substitution = getBestSubstitution(
        defs,
        containingClass,
        scopeStack,
        true,
        Set.of(),
        groupNames,
        mt.methodDescriptor().ret,
        computeConstantValue(constExprent)
      );
      if (substitution instanceof SubstitutionResult.Expr expr) {
        ExpressionToExprent converter = new ExpressionToExprent(null, null);
        expr.expr().accept(converter);
        attribute.setDefaultValue(converter.result);
        return true;
      } else if (substitution instanceof SubstitutionResult.Format format) {
        return applyFormatToConst(constExprent, format.format());
      } else {
        return false;
      }
    } else {
      return substituteExprentsInAnnotation(defs, attribute.getDefaultValue(), containingClass, scopeStack, Set.of());
    }
  }

  private static boolean applyFormatToConst(Exprent exprent, GroupFormat format) {
    ConstExprent constExprent;
    if (exprent instanceof ConstExprent c) {
      constExprent = c;
    } else if (exprent instanceof FunctionExprent functionExprent && functionExprent.getFuncType() == FunctionExprent.FunctionType.NEG && functionExprent.getLstOperands().get(0) instanceof ConstExprent c) {
      constExprent = c;
    } else {
      return false;
    }

    boolean res = false;

    if (format == GroupFormat.CHAR) {
      if (DataTypeUtils.fromVarType(constExprent.getConstType()) == DataType.INT && !constExprent.getConstType().equals(VarType.VARTYPE_CHAR)) {
        if (DataTypeUtils.tryCastExact(constExprent.getValue(), DataType.CHAR) != null) {
          constExprent.setConstType(VarType.VARTYPE_CHAR);
          constExprent.setConstTypeForced(true);
          res = true;
        }
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
        constExprent.setConstTypeForced(true);
      }
      constExprent.setRadix(radix);
      res = true;
    }

    return res;
  }

  @Nullable
  private static SubstitutionResult getBestSubstitution(
    UnpickDefinitions defs,
    String containingClass,
    List<GroupScope> scopeStack,
    boolean isStatic,
    Set<String> disallowedFieldRefs,
    Set<String> groupNames,
    VarType type,
    Object constantValue
  ) {
    DataType constantType = DataTypeUtils.getDataType(constantValue);
    MutableObject<Boolean> isReplacementExprFromVineflower = MutableObject.of(false);
    MutableObject<Expression> replacementExpr = MutableObject.of(null);
    Map<Long, Expression> replacementFlags = new LinkedHashMap<>();
    Set<Long> replacementFlagsFromVineflower = new HashSet<>();
    MutableObject<GroupFormat> bestFormat = MutableObject.of(null);

    long flagsMask = constantType == DataType.LONG ? 0xffff_ffff_ffff_ffffL : 0xffff_ffffL;
    Long targetFlags = getAsFlags(constantValue, flagsMask);

    forEachGroupWithScope(getGroupData(defs, groupNames, type), scopeStack, (groupData, scopedData) -> {
      if (bestFormat.get() == null) {
        bestFormat.set(scopedData.format);
      }

      if (groupData.isFlags && targetFlags != null && targetFlags != 0 && targetFlags != flagsMask /* -1 */) {
        scopedData.constantReplacementMap.forEach((value, replacementData) -> {
          Long flag = getAsFlags(value, flagsMask);
          if (flag != null) {
            if (!referencesDisallowedField(replacementData.replacementExpr(), containingClass, disallowedFieldRefs)) {
              if (!referencesDisallowedInstanceField(replacementData.replacementExpr(), containingClass, isStatic)) {
                boolean compatibleTypes = replacementData.strict() ? groupData.dataType == constantType : DataTypeUtils.isAssignable(constantType, groupData.dataType);
                if (compatibleTypes) {
                  if (replacementFlagsFromVineflower.contains(flag) && !replacementData.isFromVineflower()) {
                    replacementFlagsFromVineflower.remove(flag);
                    replacementFlags.put(flag, replacementData.replacementExpr());
                  } else if (!replacementFlags.containsKey(flag) && replacementData.isFromVineflower()) {
                    replacementFlagsFromVineflower.add(flag);
                    replacementFlags.put(flag, replacementData.replacementExpr());
                  } else {
                    replacementFlags.putIfAbsent(flag, replacementData.replacementExpr());
                  }
                }
              }
            }
          }
        });
      } else {
        UnpickDefinitions.ConstantReplacementData replacementData = scopedData.constantReplacementMap.get(DataTypeUtils.tryCastExact(constantValue, groupData.dataType));
        if (replacementData != null && !referencesDisallowedField(replacementData.replacementExpr(), containingClass, disallowedFieldRefs)) {
          if (!referencesDisallowedInstanceField(replacementData.replacementExpr(), containingClass, isStatic)) {
            boolean compatibleTypes = replacementData.strict() ? groupData.dataType == constantType : DataTypeUtils.isAssignable(constantType, groupData.dataType);
            if (compatibleTypes) {
              if (replacementExpr.get() == null || (isReplacementExprFromVineflower.get() && !replacementData.isFromVineflower())) {
                replacementExpr.set(replacementData.replacementExpr());
                isReplacementExprFromVineflower.set(replacementData.isFromVineflower());
              }
            }
          }
        }
      }
    });

    if (replacementExpr.get() != null) {
      return new SubstitutionResult.Expr(replacementExpr.get());
    } else if (!replacementFlags.isEmpty()) {
      assert targetFlags != null;
      Expression flagsExpr = getBestFlagsExpression(replacementFlags, constantType, targetFlags, flagsMask, bestFormat.get());
      if (flagsExpr != null) {
        return new SubstitutionResult.Expr(flagsExpr);
      }
    }

    return bestFormat.get() == null ? null : new SubstitutionResult.Format(bestFormat.get());
  }

  @Nullable
  private static Long getAsFlags(Object constantValue, long mask) {
    Long targetLong = (Long) DataTypeUtils.tryCastExact(constantValue, DataType.LONG);
    return targetLong == null ? null : targetLong & mask;
  }

  @Nullable
  private static Expression getBestFlagsExpression(
    Map<Long, Expression> replacementData,
    DataType constantType,
    long targetFlags,
    long flagsMask,
    GroupFormat bestFormat
  ) {
    List<Expression> positiveSet = new ArrayList<>();
    long positiveResidual = getMinimalFlags(targetFlags, replacementData, positiveSet);
    List<Expression> negativeSet = new ArrayList<>();
    long negativeResidual = getMinimalFlags(~targetFlags & flagsMask, replacementData, negativeSet);

    if (negativeResidual == 0 && !negativeSet.isEmpty() && (positiveResidual != 0 || negativeSet.size() < positiveSet.size())) {
      Expression resultExpression = negativeSet.get(0);
      for (int i = 1; i < negativeSet.size(); i++) {
        resultExpression = new BinaryExpression(resultExpression, negativeSet.get(i), BinaryExpression.Operator.BIT_OR);
      }
      resultExpression = new UnaryExpression(resultExpression, UnaryExpression.Operator.BIT_NOT);
      return resultExpression;
    } else {
      if (positiveSet.isEmpty()) {
        return null;
      }

      Expression resultExpression = positiveSet.get(0);
      for (int i = 1; i < positiveSet.size(); i++) {
        resultExpression = new BinaryExpression(resultExpression, positiveSet.get(i), BinaryExpression.Operator.BIT_OR);
      }

      if (positiveResidual != 0) {
        Expression residualExpr;
        if (bestFormat == GroupFormat.CHAR && DataTypeUtils.tryCastExact(positiveResidual, DataType.CHAR) != null) {
          residualExpr = new LiteralExpression(new Literal.Character((char) positiveResidual));
        } else {
          int radix;
          if (bestFormat == null) {
            radix = 16;
          } else {
            radix = switch (bestFormat) {
              case BINARY -> 2;
              case OCTAL -> 8;
              case DECIMAL -> 10;
              default -> 16;
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

      return resultExpression;
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

    Set<String> groupNames = UnpickTargetUtils.getFieldGroups(defs, cl.qualifiedName, fd);

    Set<String> disallowedFieldRefs = cl.getFields().stream()
      .dropWhile(f -> f != fd)
      .map(StructField::getName)
      .collect(Collectors.toSet());

    // We use a dumber method of getting a scope stack for fields. This is because we need it to work on external classes
    // we're not decompiling and therefore haven't analyzed. It doesn't pick up on lambda method scopes, but that's okay
    // as fields declared inside a class inside a lambda can't be referenced from outside the lambda anyway so the impact
    // is limited to a small number of rare cases that likely don't matter much.
    List<GroupScope> scopeStack = new ArrayList<>(4);
    StructClass c = cl;
    while (true) {
      scopeStack.add(new GroupScope.Method(c.qualifiedName, containingMethodName, containingMethodDesc));
      scopeStack.add(new GroupScope.Class(c.qualifiedName));

      StructEnclosingMethodAttribute enclosingMethodAttr = c.getAttribute(StructGeneralAttribute.ATTRIBUTE_ENCLOSING_METHOD);
      if (enclosingMethodAttr == null) {
        break;
      }
      c = DecompilerContext.getStructContext().getClass(enclosingMethodAttr.getClassName());
      if (c == null) {
        break;
      }
      if (enclosingMethodAttr.getMethodName() != null) {
        scopeStack.add(new GroupScope.Method(c.qualifiedName, enclosingMethodAttr.getMethodName(), enclosingMethodAttr.getMethodDescriptor()));
      }
      scopeStack.add(new GroupScope.Class(c.qualifiedName));
    }
    if (cl.qualifiedName.contains("/")) {
      scopeStack.add(new GroupScope.Package(cl.qualifiedName.substring(0, cl.qualifiedName.lastIndexOf('/'))));
    }
    Collections.reverse(scopeStack);

    return getBestSubstitution(
      defs,
      cl.qualifiedName,
      scopeStack,
      isStatic,
      disallowedFieldRefs,
      groupNames,
      new VarType(fd.getDescriptor()),
      constantValue
    );
  }

  @Nullable
  private static StructClass getOuterClass(StructClass innerClass) {
    int dollarIndex = innerClass.qualifiedName.lastIndexOf('$');
    if (dollarIndex <= innerClass.qualifiedName.lastIndexOf('/')) {
      return null;
    }
    return DecompilerContext.getStructContext().getClass(innerClass.qualifiedName.substring(0, dollarIndex));
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

  private static boolean referencesDisallowedField(Expression expression, String className, Set<String> disallowedFieldRefs) {
    MutableObject<Boolean> result = MutableObject.of(false);
    expression.accept(new ExpressionVisitor() {
      @Override
      public void visitFieldExpression(FieldExpression fieldExpression) {
        if (fieldExpression.className.replace('.', '/').equals(className) && disallowedFieldRefs.contains(fieldExpression.fieldName)) {
          result.set(true);
        }
      }
    });
    return result.get();
  }

  // TODO: handle non-static field substitutions for Outer.this and custom variables (the information is present in the bytecode but not available this late in decompilation)
  private static boolean referencesDisallowedInstanceField(Expression expression, String containingClass, boolean isStatic) {
    MutableObject<Boolean> result = MutableObject.of(false);
    expression.accept(new ExpressionVisitor() {
      @Override
      public void visitFieldExpression(FieldExpression fieldExpression) {
        if (fieldExpression.isStatic) {
          return;
        }
        if (isStatic || !fieldExpression.className.replace('.', '/').equals(containingClass)) {
          result.set(true);
        }
      }
    });
    return result.get();
  }

  private static List<UnpickDefinitions.GroupData> getGroupData(UnpickDefinitions defs, Set<String> groupNames, VarType varType) {
    if (!groupNames.isEmpty()) {
      return groupNames.stream().map(defs.groups::get).filter(Objects::nonNull).toList();
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

  private static void forEachGroupWithScope(
    Collection<UnpickDefinitions.GroupData> groupDatas,
    List<GroupScope> scopeStack,
    BiConsumer<UnpickDefinitions.GroupData, UnpickDefinitions.ScopedData> processor
  ) {
    for (int i = scopeStack.size() - 1; i >= 0; i--) {
      for (UnpickDefinitions.GroupData groupData : groupDatas) {
        GroupScope scope = scopeStack.get(i);
        if (scope instanceof GroupScope.Package pkg) {
          UnpickDefinitions.ScopedData packageData = groupData.packageScopes.get(pkg.packageName());
          if (packageData != null) {
            processor.accept(groupData, packageData);
          }
        } else if (scope instanceof GroupScope.Class clazz) {
          UnpickDefinitions.ScopedData classData = groupData.classScopes.get(clazz.className());
          if (classData != null) {
            processor.accept(groupData, classData);
          }
        } else if (scope instanceof GroupScope.Method method) {
          UnpickDefinitions.ScopedData methodData = groupData.methodScopes.get(InterpreterUtil.makeUniqueKey(method.className(), method.methodName(), method.methodDesc()));
          if (methodData != null) {
            processor.accept(groupData, methodData);
          }
        } else {
          throw new UnsupportedOperationException("Unknown scope type " + scope.getClass());
        }
      }
    }

    for (UnpickDefinitions.GroupData groupData : groupDatas) {
      processor.accept(groupData, groupData.globalScope);
    }
  }

  @Nullable
  private static Object computeConstantValue(Exprent exprent) {
    if (exprent instanceof ConstExprent constExprent) {
      if (constExprent.getConstType().equals(VarType.VARTYPE_CLASS)) {
        return new ClassLiteral(constExprent.getValue().toString());
      } else {
        return constExprent.getValue();
      }
    } else if (exprent instanceof FunctionExprent functionExprent) {
      if (functionExprent.getFuncType() == FunctionExprent.FunctionType.NEG && functionExprent.getLstOperands().get(0) instanceof ConstExprent constExprent) {
        Object value = computeConstantValue(constExprent);
        DataType valueType = DataTypeUtils.getDataType(value);
        Double doubleValue = (Double) DataTypeUtils.tryCastExact(value, DataType.DOUBLE);
        if (doubleValue != null) {
          return DataTypeUtils.tryCast(-doubleValue, valueType);
        }
        Long longValue = (Long) DataTypeUtils.tryCastExact(value, DataType.LONG);
        if (longValue != null) {
          return DataTypeUtils.tryCast(-longValue, valueType);
        }
      }
    } else if (exprent instanceof FieldExprent fieldExprent) {
      if (fieldExprent.getName().equals("TYPE") && ExprUtil.PRIMITIVE_TYPES.containsKey(fieldExprent.getClassname())) {
        String primitiveDesc = switch (fieldExprent.getClassname()) {
          case "java/lang/Byte" -> "B";
          case "java/lang/Character" -> "C";
          case "java/lang/Double" -> "D";
          case "java/lang/Float" -> "F";
          case "java/lang/Integer" -> "I";
          case "java/lang/Long" -> "J";
          case "java/lang/Short" -> "S";
          case "java/lang/Void" -> "V";
          case "java/lang/Boolean" -> "Z";
          default -> throw new IllegalStateException("Unknown primitive type " + fieldExprent.getClassname());
        };
        return new ClassLiteral(primitiveDesc);
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
