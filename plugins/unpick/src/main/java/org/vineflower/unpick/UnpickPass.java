package org.vineflower.unpick;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.plugin.pass.ClassPass;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassWriter;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchHeadExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.TypeAnnotation;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.YieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMember;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationParameterAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructMethodParametersAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructTypeAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.Key;
import org.jetbrains.java.decompiler.util.MutableObject;
import org.jetbrains.java.decompiler.util.Pair;
import org.jetbrains.java.decompiler.util.StatementIterator;
import org.vineflower.unpick.parser.UnpickV3Reader;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.GroupFormat;
import org.vineflower.unpick.parser.tree.TargetAnnotation;
import org.vineflower.unpick.parser.tree.TargetField;
import org.vineflower.unpick.parser.tree.TargetMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnpickPass implements ClassPass {
  private static final Key<UnpickDefinitions> DEFINITIONS_KEY = Key.of("unpick-definitions");

  @Override
  public boolean run(ClassesProcessor.ClassNode classNode) {
    UnpickDefinitions defs = loadDefinitions();
    if (defs.isEmpty()) {
      return false;
    }

    Map<Integer, String> exprentGroups = new HashMap<>();
    Map<Pair<String, VarVersionPair>, String> varGroups = new HashMap<>();

    identifyTargets(defs, classNode, exprentGroups, varGroups);
    List<ClassesProcessor.ClassNode> classStack = new ArrayList<>(1);
    while (propagateGroups(classNode, classStack, exprentGroups, varGroups)) {
      // keep propagating while changed
    }
    return substituteConstants(defs, classNode, exprentGroups);
  }

  private static void identifyTargets(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    Map<Integer, String> exprentGroups,
    Map<Pair<String, VarVersionPair>, String> varGroups
  ) {
    for (MethodWrapper method : classNode.getWrapper().getMethods()) {
      String returnGroup = identifyContainingMethodTargets(defs, classNode, method, varGroups);

      StatementIterator.iterate(method.root, exprent -> {
        if (exprent instanceof ExitExprent exitExprent) {
          if (exitExprent.getExitType() == ExitExprent.Type.RETURN) {
            if (exitExprent.getValue() != null) {
              exprentGroups.putIfAbsent(exitExprent.getValue().id, returnGroup);
            }
          }
        } else if (exprent instanceof FieldExprent fieldExprent) {
          identifyFieldTarget(defs, exprentGroups, fieldExprent);
        } else if (exprent instanceof InvocationExprent invocationExprent) {
          identifyInvocationTargets(defs, exprentGroups, invocationExprent, invocationExprent);
        } else if (exprent instanceof NewExprent newExprent) {
          if (newExprent.getConstructor() != null) {
            identifyInvocationTargets(defs, exprentGroups, newExprent.getConstructor(), newExprent);
          }
        }
        // TODO: pattern exprents
        return 0;
      });
    }

    for (ClassesProcessor.ClassNode nestedClass : classNode.nested) {
      identifyTargets(defs, nestedClass, exprentGroups, varGroups);
    }
  }

  @Nullable
  private static String identifyContainingMethodTargets(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    MethodWrapper method,
    Map<Pair<String, VarVersionPair>, String> varGroups
  ) {
    String methodKey = InterpreterUtil.makeUniqueKey(classNode.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor());
    MutableObject<String> returnGroup = MutableObject.of(null);
    processSuperMethods(classNode.classStruct, method.methodStruct, mt -> {
      String superMethodKey = InterpreterUtil.makeUniqueKey(mt.getClassQualifiedName(), mt.getName(), mt.getDescriptor());
      List<List<AnnotationExprent>> paramAnnotations = getParameterAnnotations(mt);
      TargetMethod targetMethod = defs.targetMethods.get(superMethodKey);
      int varIndex = mt.hasModifier(CodeConstants.ACC_STATIC) ? 0 : 1;
      VarType[] params = mt.methodDescriptor().params;
      for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
        if (targetMethod != null) {
          String paramGroup = targetMethod.paramGroups().get(paramIndex);
          if (paramGroup != null) {
            varGroups.putIfAbsent(Pair.of(methodKey, new VarVersionPair(varIndex, 0)), paramGroup);
          }
        }
        if (paramIndex < paramAnnotations.size()) {
          for (AnnotationExprent ann : paramAnnotations.get(paramIndex)) {
            TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
            if (targetAnnotation != null) {
              varGroups.putIfAbsent(Pair.of(methodKey, new VarVersionPair(varIndex, 0)), targetAnnotation.groupName());
            }
          }
        }
        varIndex += params[paramIndex].stackSize;
      }

      if (targetMethod != null && targetMethod.returnGroup() != null && returnGroup.get() == null) {
        returnGroup.set(targetMethod.returnGroup());
      }

      if (returnGroup.get() == null) {
        for (AnnotationExprent ann : getMemberAnnotations(mt, TypeAnnotation.METHOD_RETURN_TYPE)) {
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

  private static void identifyFieldTarget(UnpickDefinitions defs, Map<Integer, String> exprentGroups, FieldExprent fieldExprent) {
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
        exprentGroups.putIfAbsent(fieldExprent.id, targetField.groupName());
      } else {
        for (AnnotationExprent ann : getMemberAnnotations(resolvedField, TypeAnnotation.FIELD)) {
          TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
          if (targetAnnotation != null) {
            exprentGroups.putIfAbsent(fieldExprent.id, targetAnnotation.groupName());
          }
        }
      }
    }
  }

  private static void identifyInvocationTargets(UnpickDefinitions defs, Map<Integer, String> exprentGroups, InvocationExprent invocationExprent, Exprent treeExprent) {
    StructClass resolvedClass = DecompilerContext.getStructContext().getClass(invocationExprent.getClassname());
    if (resolvedClass != null) {
      processSuperMethods(resolvedClass, InterpreterUtil.makeUniqueKey(invocationExprent.getName(), invocationExprent.getStringDescriptor()), mt -> {
        String targetMethodKey = InterpreterUtil.makeUniqueKey(mt.getClassQualifiedName(), mt.getName(), mt.getDescriptor());
        TargetMethod targetMethod = defs.targetMethods.get(targetMethodKey);
        if (targetMethod != null) {
          targetMethod.paramGroups().forEach((paramIndex, groupName) -> {
            if (paramIndex < invocationExprent.getLstParameters().size()) {
              exprentGroups.putIfAbsent(invocationExprent.getLstParameters().get(paramIndex).id, groupName);
            }
          });
          if (targetMethod.returnGroup() != null) {
            exprentGroups.putIfAbsent(treeExprent.id, targetMethod.returnGroup());
          }
        }

        List<List<AnnotationExprent>> parameterAnnotations = getParameterAnnotations(mt);
        for (int paramIndex = 0; paramIndex < Math.min(parameterAnnotations.size(), invocationExprent.getLstParameters().size()); paramIndex++) {
          for (AnnotationExprent ann : parameterAnnotations.get(paramIndex)) {
            TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
            if (targetAnnotation != null) {
              exprentGroups.putIfAbsent(invocationExprent.getLstParameters().get(paramIndex).id, targetAnnotation.groupName());
            }
          }
        }

        for (AnnotationExprent ann : getMemberAnnotations(mt, TypeAnnotation.METHOD_RETURN_TYPE)) {
          TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
          if (targetAnnotation != null) {
            exprentGroups.putIfAbsent(treeExprent.id, targetAnnotation.groupName());
          }
        }
      }, new HashSet<>());
    }
  }

  private static void processSuperMethods(StructClass cl, StructMethod mt, Consumer<StructMethod> processor) {
    String methodKey = InterpreterUtil.makeUniqueKey(mt.getName(), mt.getDescriptor());
    processSuperMethods(cl, methodKey, processor, new HashSet<>());
  }

  // TODO: handle bridges?
  private static void processSuperMethods(StructClass cl, String methodKey, Consumer<StructMethod> processor, Set<String> visitedClasses) {
    if (!visitedClasses.add(cl.qualifiedName)) {
      return;
    }

    StructMethod mt = cl.getMethod(methodKey);
    if (mt != null) {
      processor.accept(mt);
      if (mt.getName().startsWith("<") || mt.hasModifier(CodeConstants.ACC_PRIVATE) || mt.hasModifier(CodeConstants.ACC_STATIC)) {
        return;
      }
    }

    if (cl.superClass != null) {
      StructClass superclass = DecompilerContext.getStructContext().getClass(cl.superClass.getString());
      if (superclass != null) {
        processSuperMethods(superclass, methodKey, processor, visitedClasses);
      }
    }

    for (String interfaceName : cl.getInterfaceNames()) {
      StructClass itf = DecompilerContext.getStructContext().getClass(interfaceName);
      if (itf != null) {
        processSuperMethods(itf, methodKey, processor, visitedClasses);
      }
    }
  }

  private static List<List<AnnotationExprent>> getParameterAnnotations(StructMethod mt) {
    List<List<AnnotationExprent>> result = new ArrayList<>();

    for (Key<?> paramAnnotationKey : ClassWriter.PARAMETER_ANNOTATION_ATTRIBUTES) {
      var paramAnnAttribute = (StructAnnotationParameterAttribute) mt.getAttribute(paramAnnotationKey);
      if (paramAnnAttribute != null) {
        List<List<AnnotationExprent>> paramAnnotations = paramAnnAttribute.getParamAnnotations();
        while (result.size() < paramAnnotations.size()) {
          result.add(new ArrayList<>());
        }
        for (int i = 0; i < paramAnnotations.size(); i++) {
          result.get(i).addAll(paramAnnotations.get(i));
        }
      }
    }

    for (Key<?> typeAnnotationKey : ClassWriter.TYPE_ANNOTATION_ATTRIBUTES) {
      var typeAnnAttribute = (StructTypeAnnotationAttribute) mt.getAttribute(typeAnnotationKey);
      if (typeAnnAttribute != null) {
        for (TypeAnnotation typeAnnotation : typeAnnAttribute.getAnnotations()) {
          if (typeAnnotation.getTargetType() == TypeAnnotation.METHOD_PARAMETER && typeAnnotation.path.length == 0) {
            while (result.size() <= typeAnnotation.getIndex()) {
              result.add(new ArrayList<>());
            }
            result.get(typeAnnotation.getIndex()).add(typeAnnotation.getAnnotation());
          }
        }
      }
    }

    // add indexes for synthetic params
    StructMethodParametersAttribute methodParams = mt.getAttribute(StructGeneralAttribute.ATTRIBUTE_METHOD_PARAMETERS);
    if (methodParams != null) {
      List<StructMethodParametersAttribute.Entry> entries = methodParams.getEntries();
      for (int i = 0; i < entries.size(); i++) {
        if ((entries.get(i).myAccessFlags & CodeConstants.ACC_SYNTHETIC) != 0) {
          result.add(i, new ArrayList<>());
        }
      }
    }

    return result;
  }

  private static List<AnnotationExprent> getMemberAnnotations(StructMember member, int typeAnnotationTarget) {
    List<AnnotationExprent> result = new ArrayList<>();

    for (Key<?> annotationKey : ClassWriter.ANNOTATION_ATTRIBUTES) {
      var annAttribute = (StructAnnotationAttribute) member.getAttribute(annotationKey);
      if (annAttribute != null) {
        result.addAll(annAttribute.getAnnotations());
      }
    }

    for (Key<?> typeAnnotationKey : ClassWriter.TYPE_ANNOTATION_ATTRIBUTES) {
      var typeAnnAttribute = (StructTypeAnnotationAttribute) member.getAttribute(typeAnnotationKey);
      if (typeAnnAttribute != null) {
        for (TypeAnnotation typeAnnotation : typeAnnAttribute.getAnnotations()) {
          if (typeAnnotation.getTargetType() == typeAnnotationTarget && typeAnnotation.path.length == 0) {
            result.add(typeAnnotation.getAnnotation());
          }
        }
      }
    }

    return result;
  }

  private static boolean propagateGroups(
    ClassesProcessor.ClassNode classNode,
    List<ClassesProcessor.ClassNode> classStack,
    Map<Integer, String> exprentGroups,
    Map<Pair<String, VarVersionPair>, String> varGroups
  ) {
    boolean res = false;
    classStack.add(classNode);

    for (MethodWrapper method : classNode.getWrapper().getMethods()) {
      res |= propagateGroups(method, classStack, exprentGroups, varGroups);
    }

    for (ClassesProcessor.ClassNode nestedClass : classNode.nested) {
      res |= propagateGroups(nestedClass, classStack, exprentGroups, varGroups);
    }

    classStack.remove(classStack.size() - 1);
    return res;
  }

  private static boolean propagateGroups(
    MethodWrapper method,
    List<ClassesProcessor.ClassNode> classStack,
    Map<Integer, String> exprentGroups,
    Map<Pair<String, VarVersionPair>, String> varGroups
  ) {
    MutableObject<Boolean> changed = MutableObject.of(false);

    StatementIterator.iterate(method.root, exprent -> {
      if (exprent instanceof VarExprent varExprent) {
        VarVersionPair var = varExprent.getVarVersionPair();
        MethodWrapper md = method;
        Pair<String, VarVersionPair> globalVarId = null;
        int classStackIndex = classStack.size() - 1;
        while (true) {
          var varSource = md.varproc.getVarSource(var);
          if (varSource == null) {
            globalVarId = Pair.of(InterpreterUtil.makeUniqueKey(md.classStruct.qualifiedName, md.methodStruct.getName(), md.methodStruct.getDescriptor()), var);
            break;
          }

          classStackIndex--;
          if (classStackIndex < 0) {
            break;
          }

          var = varSource.b;
          String[] nameAndDesc = varSource.a.split(" ", 2);
          md = classStack.get(classStackIndex).getWrapper().getMethodWrapper(nameAndDesc[0], nameAndDesc[1]);
        }

        if (globalVarId != null) {
          String groupName = varGroups.get(globalVarId);
          if (groupName == null) {
            groupName = exprentGroups.get(varExprent.id);
          }
          if (groupName != null) {
            tryAssignGroup(changed, exprentGroups, varExprent, groupName);
            if (!groupName.equals(varGroups.putIfAbsent(globalVarId, groupName))) {
              changed.set(true);
            }
          }
        }
      } else if (exprent instanceof FunctionExprent functionExprent) {
        switch (functionExprent.getFuncType()) {
          case ADD, SUB, MUL, AND, OR, XOR, STR_CONCAT -> {
            String groupName = firstGroupOf(exprentGroups, functionExprent, functionExprent.getLstOperands().get(0), functionExprent.getLstOperands().get(1));
            if (groupName != null) {
              tryAssignGroup(changed, exprentGroups, functionExprent, groupName);
              tryAssignGroup(changed, exprentGroups, functionExprent.getLstOperands().get(0), groupName);
              tryAssignGroup(changed, exprentGroups, functionExprent.getLstOperands().get(1), groupName);
            }
          }
          case DIV, REM, NEG, BIT_NOT, PPI, IPP, MMI, IMM -> {
            String groupName = firstGroupOf(exprentGroups, functionExprent, functionExprent.getLstOperands().get(0));
            if (groupName != null) {
              tryAssignGroup(changed, exprentGroups, functionExprent, groupName);
              tryAssignGroup(changed, exprentGroups, functionExprent.getLstOperands().get(0), groupName);
            }
          }
          case EQ, NE, GT, LT, GE, LE -> {
            String groupName = firstGroupOf(exprentGroups, functionExprent.getLstOperands().get(0), functionExprent.getLstOperands().get(1));
            if (groupName != null) {
              tryAssignGroup(changed, exprentGroups, functionExprent.getLstOperands().get(0), groupName);
              tryAssignGroup(changed, exprentGroups, functionExprent.getLstOperands().get(1), groupName);
            }
          }
          case TERNARY -> {
            String groupName = firstGroupOf(exprentGroups, functionExprent, functionExprent.getLstOperands().get(1), functionExprent.getLstOperands().get(2));
            if (groupName != null) {
              tryAssignGroup(changed, exprentGroups, functionExprent, groupName);
              tryAssignGroup(changed, exprentGroups, functionExprent.getLstOperands().get(1), groupName);
              tryAssignGroup(changed, exprentGroups, functionExprent.getLstOperands().get(2), groupName);
            }
          }
        }
      } else if (exprent instanceof AssignmentExprent assignmentExprent) {
        boolean propagate;
        boolean propagateToRight;
        if (assignmentExprent.getCondType() == null) {
          propagate = true;
          propagateToRight = true;
        } else {
          switch (assignmentExprent.getCondType()) {
            case ADD, SUB, MUL, AND, OR, XOR, STR_CONCAT -> {
              propagate = true;
              propagateToRight = true;
            }
            case DIV, REM, SHR, USHR, SHL -> {
              propagate = true;
              propagateToRight = false;
            }
            default -> {
              propagate = false;
              propagateToRight = false;
            }
          }
        }

        if (propagate) {
          String groupName;
          if (propagateToRight) {
            groupName = firstGroupOf(exprentGroups, assignmentExprent, assignmentExprent.getLeft(), assignmentExprent.getRight());
          } else {
            groupName = firstGroupOf(exprentGroups, assignmentExprent, assignmentExprent.getLeft());
          }
          if (groupName != null) {
            tryAssignGroup(changed, exprentGroups, assignmentExprent, groupName);
            tryAssignGroup(changed, exprentGroups, assignmentExprent.getLeft(), groupName);
            if (propagateToRight) {
              tryAssignGroup(changed, exprentGroups, assignmentExprent.getRight(), groupName);
            }
          }
        }
      } else if (exprent instanceof SwitchHeadExprent switchHeadExprent) {
        List<Exprent> subjectAndCases = new ArrayList<>();
        subjectAndCases.add(switchHeadExprent.getValue());
        for (List<Exprent> caze : switchHeadExprent.getCaseValues()) {
          subjectAndCases.addAll(caze);
        }
        String groupName = firstGroupOf(exprentGroups, subjectAndCases.toArray(Exprent[]::new));
        if (groupName != null) {
          for (Exprent expr : subjectAndCases) {
            tryAssignGroup(changed, exprentGroups, expr, groupName);
          }
        }
      } else if (exprent instanceof SwitchExprent switchExprent) {
        List<Exprent> switchAndYields = new ArrayList<>();
        collectYieldExprents(switchExprent.getBacking(), switchAndYields);
        String groupName = firstGroupOf(exprentGroups, switchAndYields.toArray(Exprent[]::new));
        if (groupName != null) {
          for (Exprent expr : switchAndYields) {
            tryAssignGroup(changed, exprentGroups, expr, groupName);
          }
        }
      }
      return 0;
    });

    return changed.get();
  }

  private static void collectYieldExprents(Statement stat, List<? super YieldExprent> yields) {
    if (stat.getExprents() == null) {
      for (Statement childStat : stat.getStats()) {
        if (!(childStat instanceof SwitchStatement)) {
          collectYieldExprents(childStat, yields);
        }
      }

      for (Exprent exprent : stat.getStatExprents()) {
        for (Exprent expr : exprent.getAllExprents(true, true)) {
          if (expr instanceof YieldExprent yield) {
            yields.add(yield);
          }
        }
      }
    } else {
      for (Exprent exprent : stat.getExprents()) {
        for (Exprent expr : exprent.getAllExprents(true, true)) {
          if (expr instanceof YieldExprent yield) {
            yields.add(yield);
          }
        }
      }
    }
  }

  @Nullable
  private static String firstGroupOf(Map<Integer, String> exprentGroups, Exprent... exprents) {
    for (Exprent exprent : exprents) {
      String groupName = exprentGroups.get(exprent.id);
      if (groupName != null) {
        return groupName;
      }
    }

    return null;
  }

  private static void tryAssignGroup(MutableObject<Boolean> changed, Map<Integer, String> exprentGroups, Exprent exprent, String groupName) {
    if (!groupName.equals(exprentGroups.putIfAbsent(exprent.id, groupName))) {
      changed.set(true);
    }
  }

  private static boolean substituteConstants(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    Map<Integer, String> exprentGroups
  ) {
    boolean res = false;

    for (MethodWrapper method : classNode.getWrapper().getMethods()) {
      res |= substituteConstants(defs, method, method.root, exprentGroups);
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
    Map<Integer, String> exprentGroups
  ) {
    boolean res = false;

    if (stat.getExprents() == null) {
      for (Exprent exprent : stat.getStatExprents()) {
        res |= substituteConstants(defs, method, exprent, newExprent -> stat.replaceExprent(exprent, newExprent), exprentGroups);
      }

      for (Statement st : stat.getStats()) {
        res |= substituteConstants(defs, method, st, exprentGroups);
      }
    } else {
      for (Exprent exprent : stat.getExprents()) {
        res |= substituteConstants(defs, method, exprent, newExprent -> stat.replaceExprent(exprent, newExprent), exprentGroups);
      }
    }

    return res;
  }

  // TODO: handle casting to the right type when overload resolution is affected
  private static boolean substituteConstants(
    UnpickDefinitions defs,
    MethodWrapper method,
    Exprent exprent,
    Consumer<Exprent> replace,
    Map<Integer, String> exprentGroups
  ) {
    boolean res = false;

    for (Exprent subExprent : exprent.getAllExprents()) {
      res |= substituteConstants(defs, method, subExprent, newExprent -> exprent.replaceExprent(subExprent, newExprent), exprentGroups);
    }

    Object constantValue = computeConstantValue(exprent);
    DataType constantType = DataTypeUtils.getDataType(constantValue);

    String groupName = exprentGroups.get(exprent.id);
    List<UnpickDefinitions.GroupData> groupDatas = getGroupData(defs, groupName, getExprType(exprent));

    GroupFormat bestFormat = null;
    for (UnpickDefinitions.GroupData groupData : groupDatas) {
      for (UnpickDefinitions.ScopedData scopedData : getScopedData(groupData, method.methodStruct)) {
        if (bestFormat == null) {
          bestFormat = scopedData.format;
        }

        // TODO: flags
        UnpickDefinitions.ConstantReplacementData replacementData = scopedData.constantReplacementMap.get(constantValue);
        if (replacementData != null) {
          boolean compatibleTypes = replacementData.strict() ? groupData.dataType == constantType : DataTypeUtils.isAssignable(constantType, groupData.dataType);
          if (compatibleTypes) {
            ExpressionToExprent converter = new ExpressionToExprent(method, exprent.bytecode);
            replacementData.replacementExpr().accept(converter);
            replace.accept(converter.result);
            return true;
          }
        }
      }
    }

    if (bestFormat != null && exprent instanceof ConstExprent constExprent) {
      if (bestFormat == GroupFormat.CHAR) {
        if (constantType == DataType.INT && !constExprent.getConstType().equals(VarType.VARTYPE_CHAR)) {
          constExprent.setConstType(VarType.VARTYPE_CHAR);
          res = true;
        }
      } else {
        int radix = switch (bestFormat) {
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
    }

    return res;
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

  private static VarType getExprType(Exprent exprent) {
    VarType exprType = exprent.getExprType();
    return VarType.UNBOXING_TYPES.getOrDefault(exprType, exprType);
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
  private static List<UnpickDefinitions.ScopedData> getScopedData(UnpickDefinitions.GroupData groupData, StructMethod mt) {
    List<UnpickDefinitions.ScopedData> result = new ArrayList<>(4);

    UnpickDefinitions.ScopedData methodData = groupData.methodScopes.get(InterpreterUtil.makeUniqueKey(mt.getClassQualifiedName(), mt.getName(), mt.getDescriptor()));
    if (methodData != null) {
      result.add(methodData);
    }

    UnpickDefinitions.ScopedData classData = groupData.classScopes.get(mt.getClassQualifiedName());
    if (classData != null) {
      result.add(classData);
    }

    if (mt.getClassQualifiedName().contains("/")) {
      UnpickDefinitions.ScopedData packageData = groupData.packageScopes.get(mt.getClassQualifiedName().substring(0, mt.getClassQualifiedName().lastIndexOf('/')));
      if (packageData != null) {
        result.add(packageData);
      }
    }

    result.add(groupData.globalScope);
    return result;
  }

  // Loading in Plugin.initialize is too early, we need StructContext to be populated
  private static UnpickDefinitions loadDefinitions() {
    UnpickDefinitions defs = DecompilerContext.getContextProperty(DEFINITIONS_KEY);
    if (defs != null) {
      return defs;
    }

    defs = new UnpickDefinitions();
    DecompilerContext.setProperty(DEFINITIONS_KEY, defs);

    try {
      loadBuiltInDefinitions(defs);
      defs.canBeOverwritten = false;
      loadCustomDefinitions(defs);
    } catch (IOException e) {
      DecompilerContext.getLogger().writeMessage("Failed to load unpick definitions", e);
    }

    return defs;
  }

  private static void loadBuiltInDefinitions(UnpickDefinitions defs) throws IOException {
    if (!DecompilerContext.getOption(UnpickOptions.USE_BUILTIN_UNPICK_FILES)) {
      return;
    }

    try (InputStream jdkUnpick = UnpickPass.class.getResourceAsStream("/jdk.unpick")) {
      if (jdkUnpick == null) {
        throw new IOException("Could not find built-in unpick definitions");
      }

      new UnpickV3Reader(new InputStreamReader(jdkUnpick)).accept(defs);
    }
  }

  private static void loadCustomDefinitions(UnpickDefinitions defs) throws IOException {
    String unpickFiles = (String) DecompilerContext.getProperty(UnpickOptions.UNPICK_FILES);
    if (unpickFiles.isEmpty()) {
      return;
    }

    loadCustomDefinitions(Path.of(unpickFiles), defs);
  }

  private static void loadCustomDefinitions(Path path, UnpickDefinitions defs) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<>() {
        @Override
        @NotNull
        public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
          loadCustomDefinitions(file, defs);
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      String fileName = path.getFileName().toString();
      if (fileName.endsWith(".unpick")) {
        try (UnpickV3Reader reader = new UnpickV3Reader(Files.newBufferedReader(path))) {
          reader.accept(defs);
        }
      } else if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
          Enumeration<? extends ZipEntry> entries = zip.entries();
          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".unpick")) {
              try (InputStream stream = zip.getInputStream(entry)) {
                new UnpickV3Reader(new InputStreamReader(stream)).accept(defs);
              }
            }
          }
        }
      }
    }
  }
}
