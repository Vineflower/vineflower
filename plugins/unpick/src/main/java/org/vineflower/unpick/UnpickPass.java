package org.vineflower.unpick;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.plugin.pass.ClassPass;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassWriter;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
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
import org.jetbrains.java.decompiler.struct.attr.StructConstantValueAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructMethodParametersAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructTypeAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.Key;
import org.jetbrains.java.decompiler.util.MutableObject;
import org.jetbrains.java.decompiler.util.Pair;
import org.jetbrains.java.decompiler.util.StatementIterator;
import org.vineflower.unpick.parser.UnpickParseException;
import org.vineflower.unpick.parser.UnpickV3Reader;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.GroupFormat;
import org.vineflower.unpick.parser.tree.TargetAnnotation;
import org.vineflower.unpick.parser.tree.TargetField;
import org.vineflower.unpick.parser.tree.TargetMethod;
import org.vineflower.unpick.parser.tree.expr.Expression;
import org.vineflower.unpick.parser.tree.expr.ExpressionVisitor;
import org.vineflower.unpick.parser.tree.expr.FieldExpression;

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
import java.util.stream.Collectors;
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

    Map<Pair<String, Integer>, String> exprentGroups = new HashMap<>();
    Map<Pair<String, VarVersionPair>, String> varGroups = new HashMap<>();

    identifyTargets(defs, classNode, exprentGroups, varGroups);
    List<ClassesProcessor.ClassNode> classStack = new ArrayList<>(1);
    while (propagateGroups(classNode, classStack, exprentGroups, varGroups)) {
      // keep propagating while changed
    }
    boolean res = substituteConstants(defs, classNode, exprentGroups);
    res |= substituteConstantFieldInitializers(classNode, defs);
    return res;
  }

  private static void identifyTargets(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    Map<Pair<String, Integer>, String> exprentGroups,
    Map<Pair<String, VarVersionPair>, String> varGroups
  ) {
    for (MethodWrapper method : classNode.getWrapper().getMethods()) {
      String returnGroup = identifyContainingMethodTargets(defs, classNode, method, varGroups);
      String methodKey = InterpreterUtil.makeUniqueKey(classNode.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor());

      StatementIterator.iterate(method.root, exprent -> {
        if (exprent instanceof ExitExprent exitExprent) {
          if (exitExprent.getExitType() == ExitExprent.Type.RETURN) {
            if (exitExprent.getValue() != null) {
              exprentGroups.putIfAbsent(Pair.of(methodKey, exitExprent.getValue().id), returnGroup);
            }
          }
        } else if (exprent instanceof FieldExprent fieldExprent) {
          identifyFieldTarget(defs, methodKey, exprentGroups, fieldExprent);
        } else if (exprent instanceof InvocationExprent invocationExprent) {
          identifyInvocationTargets(defs, methodKey, exprentGroups, invocationExprent, invocationExprent);
        } else if (exprent instanceof NewExprent newExprent) {
          if (newExprent.getConstructor() != null) {
            identifyInvocationTargets(defs, methodKey, exprentGroups, newExprent.getConstructor(), newExprent);
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

  private static void identifyFieldTarget(UnpickDefinitions defs, String methodKey, Map<Pair<String, Integer>, String> exprentGroups, FieldExprent fieldExprent) {
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
        exprentGroups.putIfAbsent(Pair.of(methodKey, fieldExprent.id), targetField.groupName());
      } else {
        for (AnnotationExprent ann : getMemberAnnotations(resolvedField, TypeAnnotation.FIELD)) {
          TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
          if (targetAnnotation != null) {
            exprentGroups.putIfAbsent(Pair.of(methodKey, fieldExprent.id), targetAnnotation.groupName());
          }
        }
      }
    }
  }

  private static void identifyInvocationTargets(UnpickDefinitions defs, String methodKey, Map<Pair<String, Integer>, String> exprentGroups, InvocationExprent invocationExprent, Exprent treeExprent) {
    StructClass resolvedClass = DecompilerContext.getStructContext().getClass(invocationExprent.getClassname());
    if (resolvedClass != null) {
      processSuperMethods(resolvedClass, InterpreterUtil.makeUniqueKey(invocationExprent.getName(), invocationExprent.getStringDescriptor()), mt -> {
        String targetMethodKey = InterpreterUtil.makeUniqueKey(mt.getClassQualifiedName(), mt.getName(), mt.getDescriptor());
        TargetMethod targetMethod = defs.targetMethods.get(targetMethodKey);
        if (targetMethod != null) {
          targetMethod.paramGroups().forEach((paramIndex, groupName) -> {
            if (paramIndex < invocationExprent.getLstParameters().size()) {
              exprentGroups.putIfAbsent(Pair.of(methodKey, invocationExprent.getLstParameters().get(paramIndex).id), groupName);
            }
          });
          if (targetMethod.returnGroup() != null) {
            exprentGroups.putIfAbsent(Pair.of(methodKey, treeExprent.id), targetMethod.returnGroup());
          }
        }

        List<List<AnnotationExprent>> parameterAnnotations = getParameterAnnotations(mt);
        for (int paramIndex = 0; paramIndex < Math.min(parameterAnnotations.size(), invocationExprent.getLstParameters().size()); paramIndex++) {
          for (AnnotationExprent ann : parameterAnnotations.get(paramIndex)) {
            TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
            if (targetAnnotation != null) {
              exprentGroups.putIfAbsent(Pair.of(methodKey, invocationExprent.getLstParameters().get(paramIndex).id), targetAnnotation.groupName());
            }
          }
        }

        for (AnnotationExprent ann : getMemberAnnotations(mt, TypeAnnotation.METHOD_RETURN_TYPE)) {
          TargetAnnotation targetAnnotation = defs.targetAnnotations.get(ann.getClassName());
          if (targetAnnotation != null) {
            exprentGroups.putIfAbsent(Pair.of(methodKey, treeExprent.id), targetAnnotation.groupName());
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
    Map<Pair<String, Integer>, String> exprentGroups,
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
    Map<Pair<String, Integer>, String> exprentGroups,
    Map<Pair<String, VarVersionPair>, String> varGroups
  ) {
    MutableObject<Boolean> changed = MutableObject.of(false);
    String methodKey = InterpreterUtil.makeUniqueKey(method.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor());

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
            groupName = exprentGroups.get(Pair.of(methodKey, varExprent.id));
          }
          if (groupName != null) {
            tryAssignGroup(changed, exprentGroups, methodKey, varExprent, groupName);
            if (varGroups.putIfAbsent(globalVarId, groupName) == null) {
              changed.set(true);
            }
          }
        }
      } else if (exprent instanceof FunctionExprent functionExprent) {
        switch (functionExprent.getFuncType()) {
          case ADD, SUB, MUL, AND, OR, XOR, STR_CONCAT -> {
            String groupName = firstGroupOf(exprentGroups, methodKey, functionExprent, functionExprent.getLstOperands().get(0), functionExprent.getLstOperands().get(1));
            if (groupName != null) {
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent, groupName);
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent.getLstOperands().get(0), groupName);
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent.getLstOperands().get(1), groupName);
            }
          }
          case DIV, REM, NEG, BIT_NOT, PPI, IPP, MMI, IMM -> {
            String groupName = firstGroupOf(exprentGroups, methodKey, functionExprent, functionExprent.getLstOperands().get(0));
            if (groupName != null) {
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent, groupName);
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent.getLstOperands().get(0), groupName);
            }
          }
          case EQ, NE, GT, LT, GE, LE -> {
            String groupName = firstGroupOf(exprentGroups, methodKey, functionExprent.getLstOperands().get(0), functionExprent.getLstOperands().get(1));
            if (groupName != null) {
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent.getLstOperands().get(0), groupName);
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent.getLstOperands().get(1), groupName);
            }
          }
          case TERNARY -> {
            String groupName = firstGroupOf(exprentGroups, methodKey, functionExprent, functionExprent.getLstOperands().get(1), functionExprent.getLstOperands().get(2));
            if (groupName != null) {
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent, groupName);
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent.getLstOperands().get(1), groupName);
              tryAssignGroup(changed, exprentGroups, methodKey, functionExprent.getLstOperands().get(2), groupName);
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
            groupName = firstGroupOf(exprentGroups, methodKey, assignmentExprent, assignmentExprent.getLeft(), assignmentExprent.getRight());
          } else {
            groupName = firstGroupOf(exprentGroups, methodKey, assignmentExprent, assignmentExprent.getLeft());
          }
          if (groupName != null) {
            tryAssignGroup(changed, exprentGroups, methodKey, assignmentExprent, groupName);
            tryAssignGroup(changed, exprentGroups, methodKey, assignmentExprent.getLeft(), groupName);
            if (propagateToRight) {
              tryAssignGroup(changed, exprentGroups, methodKey, assignmentExprent.getRight(), groupName);
            }
          }
        }
      } else if (exprent instanceof SwitchHeadExprent switchHeadExprent) {
        List<Exprent> subjectAndCases = new ArrayList<>();
        subjectAndCases.add(switchHeadExprent.getValue());
        for (List<Exprent> caze : switchHeadExprent.getCaseValues()) {
          subjectAndCases.addAll(caze);
        }
        String groupName = firstGroupOf(exprentGroups, methodKey, subjectAndCases.toArray(Exprent[]::new));
        if (groupName != null) {
          for (Exprent expr : subjectAndCases) {
            tryAssignGroup(changed, exprentGroups, methodKey, expr, groupName);
          }
        }
      } else if (exprent instanceof SwitchExprent switchExprent) {
        List<Exprent> switchAndYields = new ArrayList<>();
        collectYieldExprents(switchExprent.getBacking(), switchAndYields);
        String groupName = firstGroupOf(exprentGroups, methodKey, switchAndYields.toArray(Exprent[]::new));
        if (groupName != null) {
          for (Exprent expr : switchAndYields) {
            tryAssignGroup(changed, exprentGroups, methodKey, expr, groupName);
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
  private static String firstGroupOf(Map<Pair<String, Integer>, String> exprentGroups, String methodKey, Exprent... exprents) {
    for (Exprent exprent : exprents) {
      String groupName = exprentGroups.get(Pair.of(methodKey, exprent.id));
      if (groupName != null) {
        return groupName;
      }
    }

    return null;
  }

  private static void tryAssignGroup(MutableObject<Boolean> changed, Map<Pair<String, Integer>, String> exprentGroups, String methodKey, Exprent exprent, String groupName) {
    if (exprentGroups.putIfAbsent(Pair.of(methodKey, exprent.id), groupName) == null) {
      changed.set(true);
    }
  }

  private static boolean substituteConstants(
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
      getExprType(exprent),
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
  private static boolean substituteConstantFieldInitializers(ClassesProcessor.ClassNode classNode, UnpickDefinitions defs) {
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

  private sealed interface SubstitutionResult {
    record Expr(Expression expr) implements SubstitutionResult {
    }

    record Format(GroupFormat format) implements SubstitutionResult {
    }
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
      for (UnpickDefinitions.ScopedData scopedData : getScopedData(groupData, containingClass, containingMethodName, containingMethodDesc)) {
        if (bestFormat == null) {
          bestFormat = scopedData.format;
        }

        // TODO: flags
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
      for (AnnotationExprent ann : getMemberAnnotations(fd, TypeAnnotation.FIELD)) {
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

        StructClass resolvedClass = DecompilerContext.getStructContext().getClass(fieldExpression.className);
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
        if (fieldExpression.className.equals(className) && unreferenceableFields.contains(fieldExpression.fieldName)) {
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
        } catch (UnpickParseException e) {
          DecompilerContext.getLogger().writeMessage(path + ":" + e.getMessage(), IFernflowerLogger.Severity.WARN);
        }
      } else if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
          Enumeration<? extends ZipEntry> entries = zip.entries();
          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".unpick")) {
              try (InputStream stream = zip.getInputStream(entry)) {
                new UnpickV3Reader(new InputStreamReader(stream)).accept(defs);
              } catch (UnpickParseException e) {
                DecompilerContext.getLogger().writeMessage(path + "!/" + entry.getName() + ":" + e.getMessage(), IFernflowerLogger.Severity.WARN);
              }
            }
          }
        }
      }
    }
  }
}
