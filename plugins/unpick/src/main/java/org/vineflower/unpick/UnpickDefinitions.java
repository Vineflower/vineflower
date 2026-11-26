package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.GroupDefinition;
import org.vineflower.unpick.parser.tree.GroupFormat;
import org.vineflower.unpick.parser.tree.GroupScope;
import org.vineflower.unpick.parser.tree.TargetAnnotation;
import org.vineflower.unpick.parser.tree.TargetField;
import org.vineflower.unpick.parser.tree.TargetMethod;
import org.vineflower.unpick.parser.tree.UnpickV3Visitor;
import org.vineflower.unpick.parser.tree.expr.Expression;
import org.vineflower.unpick.parser.tree.expr.ExpressionTransformer;
import org.vineflower.unpick.parser.tree.expr.ExpressionVisitor;
import org.vineflower.unpick.parser.tree.expr.FieldExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnpickDefinitions extends UnpickV3Visitor {
  public boolean areDefinitionsFromVineflower = true;
  public final Map<DataType, GroupData> defaultGroups = new HashMap<>();
  public final Map<String, GroupData> groups = new HashMap<>();
  public final Map<String, TargetField> targetFields = new HashMap<>();
  public final Map<String, TargetMethod> targetMethods = new HashMap<>();
  public final Map<String, TargetAnnotation> targetAnnotations = new HashMap<>();

  public boolean isEmpty() {
    return defaultGroups.isEmpty() && groups.isEmpty();
  }

  @Override
  public void visitGroupDefinition(GroupDefinition groupDefinition) {
    GroupData existingGroup = groupDefinition.name() == null
      ? defaultGroups.computeIfAbsent(groupDefinition.dataType(), k -> new GroupData(groupDefinition.dataType(), groupDefinition.flags()))
      : groups.computeIfAbsent(groupDefinition.name(), k -> new GroupData(groupDefinition.dataType(), groupDefinition.flags()));
    String groupDisplayName = groupDefinition.name() == null ? "<default>" : groupDefinition.name();

    if (existingGroup.isFlags != groupDefinition.flags()) {
      DecompilerContext.getLogger().writeMessage("Flags mismatch for group " + groupDisplayName, IFernflowerLogger.Severity.WARN);
      return;
    }
    if (existingGroup.dataType != groupDefinition.dataType()) {
      DecompilerContext.getLogger().writeMessage("Data type mismatch for group " + groupDisplayName, IFernflowerLogger.Severity.WARN);
      return;
    }

    if (groupDefinition.scopes().isEmpty()) {
      addScopedGroupDefinition(groupDefinition, existingGroup.globalScope, groupDisplayName);
    } else {
      for (GroupScope scope : groupDefinition.scopes()) {
        ScopedData scopeData;
        if (scope instanceof GroupScope.Package packageScope) {
          scopeData = existingGroup.packageScopes.computeIfAbsent(packageScope.packageName().replace('.', '/'), k -> new ScopedData());
        } else if (scope instanceof GroupScope.Class classScope) {
          scopeData = existingGroup.classScopes.computeIfAbsent(classScope.className().replace('.', '/'), k -> new ScopedData());
        } else if (scope instanceof GroupScope.Method methodScope) {
          scopeData = existingGroup.methodScopes.computeIfAbsent(InterpreterUtil.makeUniqueKey(methodScope.className().replace('.', '/'), methodScope.methodName(), methodScope.methodDesc()), k -> new ScopedData());
        } else {
          throw new IllegalStateException("Unexpected group scope type: " + scope.getClass().getName());
        }

        addScopedGroupDefinition(groupDefinition, scopeData, groupDisplayName);
      }
    }
  }

  private void addScopedGroupDefinition(GroupDefinition groupDefinition, ScopedData scopeData, String groupDisplayName) {
    if (groupDefinition.format() != null) {
      if (scopeData.format != groupDefinition.format() && scopeData.format != null) {
        DecompilerContext.getLogger().writeMessage("Format mismatch for group " + groupDisplayName, IFernflowerLogger.Severity.WARN);
        return;
      }

      scopeData.format = groupDefinition.format();
    }

    for (Expression expression : groupDefinition.constants()) {
      for (Expression nonWildcardExpression : replaceWildcardIfNecessary(expression, groupDefinition.dataType(), groupDisplayName)) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Object value;
        try {
          nonWildcardExpression.accept(evaluator);
          value = ExpressionEvaluator.cast(evaluator.result, groupDefinition.dataType());
        } catch (ExpressionEvaluator.EvaluationException e) {
          DecompilerContext.getLogger().writeMessage("Could not evaluate expression " + UnpickUtils.expressionToString(nonWildcardExpression) + " in group " + groupDisplayName + ": " + e.getMessage(), IFernflowerLogger.Severity.WARN);
          continue;
        }

        ConstantReplacementData existingConstant = scopeData.constantReplacementMap.put(value, new ConstantReplacementData(groupDefinition.strict(), nonWildcardExpression, areDefinitionsFromVineflower));
        if (existingConstant != null && (areDefinitionsFromVineflower || !existingConstant.isFromVineflower)) {
          DecompilerContext.getLogger().writeMessage("Duplicate constant in group " + groupDisplayName + ": " + value, IFernflowerLogger.Severity.WARN);
        }
      }
    }
  }

  private List<Expression> replaceWildcardIfNecessary(Expression expression, DataType groupType, String groupDisplayName) {
    List<FieldExpression> wildcards = findWildcardFieldExpressions(expression);
    if (wildcards.isEmpty()) {
      return List.of(expression);
    } else if (wildcards.size() > 1) {
      DecompilerContext.getLogger().writeMessage("Expression in group " + groupDisplayName + " has multiple wildcard field expressions", IFernflowerLogger.Severity.WARN);
      return List.of();
    }

    FieldExpression wildcard = wildcards.get(0);
    StructClass ownerClass = DecompilerContext.getStructContext().getClass(wildcard.className.replace('.', '/'));
    if (ownerClass == null) {
      DecompilerContext.getLogger().writeMessage("Unknown class " + wildcard.className + " in group " + groupDisplayName, IFernflowerLogger.Severity.WARN);
      return List.of();
    }

    List<Expression> replacements = new ArrayList<>();
    for (StructField field : ownerClass.getFields()) {
      if (!field.hasAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE)) {
        // TODO: scan <init> and <clinit> bytecode for constants that aren't stored in ConstantValue?
        continue;
      }

      if (field.hasModifier(CodeConstants.ACC_STATIC) != wildcard.isStatic) {
        continue;
      }

      DataType resolvedConstantType = DataTypeUtils.descriptorToDataType(field.getDescriptor());
      if (wildcard.fieldType != null) {
        if (resolvedConstantType != wildcard.fieldType) {
          continue;
        }
      } else {
        if (!DataTypeUtils.isAssignable(groupType, resolvedConstantType)) {
          continue;
        }
      }

      Expression replacement = expression.transform(new ExpressionTransformer() {
        @Override
        public Expression transformFieldExpression(FieldExpression fieldExpression) {
          if (fieldExpression.fieldName == null) {
            return new FieldExpression(fieldExpression.className, field.getName(), resolvedConstantType, fieldExpression.isStatic);
          } else {
            return super.transformFieldExpression(fieldExpression);
          }
        }
      });
      replacements.add(replacement);
    }

    if (replacements.isEmpty()) {
      DecompilerContext.getLogger().writeMessage("Could not resolve wildcard field expression " + wildcard.className + ".* in group " + groupDisplayName, IFernflowerLogger.Severity.WARN);
    }

    return replacements;
  }

  private static List<FieldExpression> findWildcardFieldExpressions(Expression expression) {
    List<FieldExpression> result = new ArrayList<>();
    expression.accept(new ExpressionVisitor() {
      @Override
      public void visitFieldExpression(FieldExpression fieldExpression) {
        if (fieldExpression.fieldName == null) {
          result.add(fieldExpression);
        }
      }
    });
    return result;
  }

  @Override
  public void visitTargetField(TargetField targetField) {
    targetFields.put(
      InterpreterUtil.makeUniqueKey(targetField.className().replace('.', '/'), targetField.fieldName(), targetField.fieldDesc()),
      targetField
    );
  }

  @Override
  public void visitTargetMethod(TargetMethod targetMethod) {
    targetMethods.put(
      InterpreterUtil.makeUniqueKey(targetMethod.className().replace('.', '/'), targetMethod.methodName(), targetMethod.methodDesc()),
      targetMethod
    );
  }

  @Override
  public void visitTargetAnnotation(TargetAnnotation targetAnnotation) {
    targetAnnotations.put(targetAnnotation.annotationName().replace('.', '/'), targetAnnotation);
  }

  public static class GroupData {
    public final DataType dataType;
    public final boolean isFlags;
    public final ScopedData globalScope = new ScopedData();
    public final Map<String, ScopedData> packageScopes = new HashMap<>();
    public final Map<String, ScopedData> classScopes = new HashMap<>();
    public final Map<String, ScopedData> methodScopes = new HashMap<>();

    public GroupData(DataType dataType, boolean isFlags) {
      this.dataType = dataType;
      this.isFlags = isFlags;
    }
  }

  public static class ScopedData {
    @Nullable
    public GroupFormat format;
    public final Map<Object, ConstantReplacementData> constantReplacementMap = new HashMap<>();
  }

  public record ConstantReplacementData(boolean strict, Expression replacementExpr, boolean isFromVineflower) {
  }
}
