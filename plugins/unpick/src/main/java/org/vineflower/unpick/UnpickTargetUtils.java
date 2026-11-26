package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.PatternExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.TypeAnnotation;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.StructRecordComponent;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.MutableObject;
import org.jetbrains.java.decompiler.util.StatementIterator;
import org.vineflower.unpick.parser.tree.TargetAnnotation;
import org.vineflower.unpick.parser.tree.TargetField;
import org.vineflower.unpick.parser.tree.TargetMethod;

import java.util.HashSet;
import java.util.List;

public final class UnpickTargetUtils {
  private UnpickTargetUtils() {
  }

  static void identifyTargets(UnpickDefinitions defs, ClassesProcessor.ClassNode classNode, GroupTracker groupTracker) {
    UnpickUtils.iterateAllMethods(classNode, (node, method, classStack) -> {
      String returnGroup = identifyContainingMethodTargets(defs, node, method, groupTracker);
      String methodKey = InterpreterUtil.makeUniqueKey(node.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor());

      StatementIterator.iterate(method.root, exprent -> {
        if (exprent instanceof ExitExprent exitExprent) {
          if (exitExprent.getExitType() == ExitExprent.Type.RETURN) {
            if (exitExprent.getValue() != null) {
              groupTracker.trySetExprentGroup(methodKey, exitExprent.getValue(), returnGroup);
            }
          }
        } else if (exprent instanceof FieldExprent fieldExprent) {
          identifyFieldTarget(defs, methodKey, groupTracker, fieldExprent);
        } else if (exprent instanceof InvocationExprent invocationExprent) {
          identifyInvocationTargets(defs, methodKey, groupTracker, invocationExprent, invocationExprent);
        } else if (exprent instanceof NewExprent newExprent) {
          if (newExprent.getConstructor() != null) {
            identifyInvocationTargets(defs, methodKey, groupTracker, newExprent.getConstructor(), newExprent);
          }
        } else if (exprent instanceof PatternExprent patternExprent) {
          identifyPatternTargets(defs, methodKey, groupTracker, patternExprent);
        }
        return 0;
      });
    });
  }

  @Nullable
  private static String identifyContainingMethodTargets(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    MethodWrapper method,
    GroupTracker groupTracker
  ) {
    String methodKey = InterpreterUtil.makeUniqueKey(classNode.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor());
    MutableObject<String> returnGroup = MutableObject.of(null);
    boolean isOriginalMethodStatic = method.methodStruct.hasModifier(CodeConstants.ACC_STATIC);
    VarType[] originalMethodParams = method.methodStruct.methodDescriptor().params;
    UnpickUtils.processSuperMethods(classNode, classNode.classStruct, method.methodStruct, mt -> {
      String superMethodKey = InterpreterUtil.makeUniqueKey(mt.getClassQualifiedName(), mt.getName(), mt.getDescriptor());
      List<List<AnnotationExprent>> paramAnnotations = UnpickUtils.getParameterAnnotations(mt);
      TargetMethod targetMethod = defs.targetMethods.get(superMethodKey);

      // The var index for parameters can differ between the super method and the original method in the case of lambdas.
      // Captured variables are added at the start, and the staticness can differ.
      int originalVarIndex = isOriginalMethodStatic ? 0 : 1;
      VarType[] params = mt.methodDescriptor().params;
      for (int i = 0; i < originalMethodParams.length - params.length; i++) {
        originalVarIndex += originalMethodParams[i].stackSize;
      }

      for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
        if (targetMethod != null) {
          String paramGroup = targetMethod.paramGroups().get(paramIndex);
          if (paramGroup != null) {
            groupTracker.trySetVarGroup(methodKey, new VarVersionPair(originalVarIndex, 0), paramGroup);
          }
        }
        if (paramIndex < paramAnnotations.size()) {
          for (AnnotationExprent ann : paramAnnotations.get(paramIndex)) {
            TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
            if (targetAnnotation != null) {
              groupTracker.trySetVarGroup(methodKey, new VarVersionPair(originalVarIndex, 0), targetAnnotation.groupName());
            }
          }
        }
        originalVarIndex += params[paramIndex].stackSize;
      }

      if (targetMethod != null && targetMethod.returnGroup() != null && returnGroup.get() == null) {
        returnGroup.set(targetMethod.returnGroup());
      }

      if (returnGroup.get() == null) {
        for (AnnotationExprent ann : UnpickUtils.getMemberAnnotations(mt, TypeAnnotation.METHOD_RETURN_TYPE)) {
          TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
          if (targetAnnotation != null) {
            returnGroup.set(targetAnnotation.groupName());
            break;
          }
        }
      }
    });

    return returnGroup.get();
  }

  private static void identifyFieldTarget(UnpickDefinitions defs, String methodKey, GroupTracker groupTracker, FieldExprent fieldExprent) {
    StructField resolvedField = null;
    StructClass ownerClass = DecompilerContext.getStructContext().getClass(fieldExprent.getClassname());
    while (ownerClass != null) {
      resolvedField = ownerClass.getField(fieldExprent.getName(), fieldExprent.getDescriptor().descriptorString);
      if (resolvedField != null) {
        break;
      }
      ownerClass = ownerClass.superClass == null ? null : DecompilerContext.getStructContext().getClass(ownerClass.superClass.getString());
    }

    if (resolvedField != null) {
      TargetField targetField = defs.targetFields.get(InterpreterUtil.makeUniqueKey(ownerClass.qualifiedName, resolvedField.getName(), resolvedField.getDescriptor()));
      if (targetField != null) {
        groupTracker.trySetExprentGroup(methodKey, fieldExprent, targetField.groupName());
      } else {
        for (AnnotationExprent ann : UnpickUtils.getMemberAnnotations(resolvedField, TypeAnnotation.FIELD)) {
          TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
          if (targetAnnotation != null) {
            groupTracker.trySetExprentGroup(methodKey, fieldExprent, targetAnnotation.groupName());
          }
        }
      }
    }
  }

  private static void identifyInvocationTargets(UnpickDefinitions defs, String methodKey, GroupTracker groupTracker, InvocationExprent invocationExprent, Exprent treeExprent) {
    StructClass resolvedClass = DecompilerContext.getStructContext().getClass(invocationExprent.getClassname());
    if (resolvedClass != null) {
      UnpickUtils.processSuperMethods(null, resolvedClass, InterpreterUtil.makeUniqueKey(invocationExprent.getName(), invocationExprent.getStringDescriptor()), mt -> {
        String targetMethodKey = InterpreterUtil.makeUniqueKey(mt.getClassQualifiedName(), mt.getName(), mt.getDescriptor());
        TargetMethod targetMethod = defs.targetMethods.get(targetMethodKey);
        if (targetMethod != null) {
          targetMethod.paramGroups().forEach((paramIndex, groupName) -> {
            if (paramIndex < invocationExprent.getLstParameters().size()) {
              groupTracker.trySetExprentGroup(methodKey, invocationExprent.getLstParameters().get(paramIndex), groupName);
            }
          });
          if (targetMethod.returnGroup() != null) {
            groupTracker.trySetExprentGroup(methodKey, treeExprent, targetMethod.returnGroup());
          }
        }

        List<List<AnnotationExprent>> parameterAnnotations = UnpickUtils.getParameterAnnotations(mt);
        for (int paramIndex = 0; paramIndex < Math.min(parameterAnnotations.size(), invocationExprent.getLstParameters().size()); paramIndex++) {
          for (AnnotationExprent ann : parameterAnnotations.get(paramIndex)) {
            TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
            if (targetAnnotation != null) {
              groupTracker.trySetExprentGroup(methodKey, invocationExprent.getLstParameters().get(paramIndex), targetAnnotation.groupName());
            }
          }
        }

        for (AnnotationExprent ann : UnpickUtils.getMemberAnnotations(mt, TypeAnnotation.METHOD_RETURN_TYPE)) {
          TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
          if (targetAnnotation != null) {
            groupTracker.trySetExprentGroup(methodKey, treeExprent, targetAnnotation.groupName());
          }
        }
      }, new HashSet<>());
    }
  }

  private static void identifyPatternTargets(UnpickDefinitions defs, String methodKey, GroupTracker groupTracker, PatternExprent patternExprent) {
    if (!(patternExprent.getData() instanceof PatternExprent.PatternData.RecordPatternData recordData)) {
      return;
    }

    List<StructRecordComponent> recordComponents = recordData.cl().getRecordComponents();
    if (recordComponents == null) {
      return;
    }

    for (int i = 0; i < Math.min(recordComponents.size(), patternExprent.getExprents().size()); i++) {
      StructRecordComponent recordComponent = recordComponents.get(i);

      String groupName = null;
      TargetMethod targetMethod = defs.targetMethods.get(InterpreterUtil.makeUniqueKey(recordData.cl().qualifiedName, recordComponent.getName(), "()" + recordComponent.getDescriptor()));
      if (targetMethod != null) {
        groupName = targetMethod.returnGroup();
      } else {
        StructMethod mt = recordData.cl().getMethod(recordComponent.getName(), "()" + recordComponent.getDescriptor());
        if (mt != null) {
          for (AnnotationExprent ann : UnpickUtils.getMemberAnnotations(mt, TypeAnnotation.METHOD_RETURN_TYPE)) {
            TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
            if (targetAnnotation != null) {
              groupName = targetAnnotation.groupName();
              break;
            }
          }
        }
      }

      if (groupName != null && patternExprent.getExprents().get(i) instanceof VarExprent varExprent) {
        groupTracker.trySetVarGroup(methodKey, varExprent.getVarVersionPair(), groupName);
      }
    }
  }
}
