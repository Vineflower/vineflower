package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassWriter;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.TypeAnnotation;
import org.jetbrains.java.decompiler.struct.StructClass;
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
import org.vineflower.unpick.parser.UnpickV3Writer;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.GroupDefinition;
import org.vineflower.unpick.parser.tree.expr.Expression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class UnpickUtils {
  private UnpickUtils() {
  }

  public static String expressionToString(Expression expr) {
    UnpickV3Writer writer = new UnpickV3Writer();
    writer.visitHeader(4);
    writer.visitGroupDefinition(GroupDefinition.Builder.global(DataType.INT).constant(expr).build());
    List<String> lines = writer.getOutput().lines().collect(Collectors.toCollection(ArrayList::new));
    while (lines.get(lines.size() - 1).isBlank()) {
      lines.remove(lines.size() - 1);
    }
    return lines.get(lines.size() - 1).trim();
  }

  // ensures lambda methods are processed with the lambda ClassNode
  static void iterateAllMethods(ClassesProcessor.ClassNode classNode, MethodConsumer consumer) {
    iterateAllMethods(classNode, consumer, new ArrayList<>(1), new HashSet<>());
  }

  private static void iterateAllMethods(ClassesProcessor.ClassNode classNode, MethodConsumer consumer, List<ClassesProcessor.ClassNode> classStack, Set<String> alreadyProcessed) {
    classStack.add(classNode);

    for (ClassesProcessor.ClassNode nested : classNode.nested) {
      iterateAllMethods(nested, consumer, classStack, alreadyProcessed);
    }

    if (classNode.type == ClassesProcessor.ClassNode.Type.LAMBDA) {
      if (classNode.classStruct.qualifiedName.equals(classNode.lambdaInformation.content_class_name)) {
        for (MethodWrapper method : classNode.getWrapper().getMethods()) {
          if (method.methodStruct.getName().equals(classNode.lambdaInformation.content_method_name) && method.methodStruct.getDescriptor().equals(classNode.lambdaInformation.content_method_descriptor)) {
            if (alreadyProcessed.add(InterpreterUtil.makeUniqueKey(classNode.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor()))) {
              consumer.process(classNode, method, classStack);
            }
          }
        }
      }
    } else {
      for (MethodWrapper method : classNode.getWrapper().getMethods()) {
        if (alreadyProcessed.add(InterpreterUtil.makeUniqueKey(classNode.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor()))) {
          consumer.process(classNode, method, classStack);
        }
      }
    }

    classStack.remove(classStack.size() - 1);
  }

  static void processSuperMethods(@Nullable ClassesProcessor.ClassNode lambdaInfo, StructClass cl, StructMethod mt, Consumer<StructMethod> processor) {
    String methodKey = InterpreterUtil.makeUniqueKey(mt.getName(), mt.getDescriptor());
    processSuperMethods(lambdaInfo, cl, methodKey, processor, new HashSet<>());
  }

  // TODO: handle bridges?
  static void processSuperMethods(@Nullable ClassesProcessor.ClassNode lambdaInfo, StructClass cl, String methodKey, Consumer<StructMethod> processor, Set<String> visitedClasses) {
    if (lambdaInfo != null && lambdaInfo.type == ClassesProcessor.ClassNode.Type.LAMBDA) {
      StructMethod mt = cl.getMethod(methodKey);
      if (mt != null) {
        processor.accept(mt);
      }
      cl = DecompilerContext.getStructContext().getClass(lambdaInfo.anonymousClassType.value);
      if (cl == null) {
        return;
      }
      methodKey = InterpreterUtil.makeUniqueKey(lambdaInfo.lambdaInformation.method_name, lambdaInfo.lambdaInformation.method_descriptor);
    }

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
        processSuperMethods(null, superclass, methodKey, processor, visitedClasses);
      }
    }

    for (String interfaceName : cl.getInterfaceNames()) {
      StructClass itf = DecompilerContext.getStructContext().getClass(interfaceName);
      if (itf != null) {
        processSuperMethods(null, itf, methodKey, processor, visitedClasses);
      }
    }
  }

  static List<List<AnnotationExprent>> getParameterAnnotations(StructMethod mt) {
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

  static List<AnnotationExprent> getMemberAnnotations(StructMember member, int typeAnnotationTarget) {
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

  static VarType getExprType(Exprent exprent) {
    VarType exprType = exprent.getExprType();
    return VarType.UNBOXING_TYPES.getOrDefault(exprType, exprType);
  }

  @FunctionalInterface
  interface MethodConsumer {
    void process(ClassesProcessor.ClassNode node, MethodWrapper method, List<ClassesProcessor.ClassNode> classStack);
  }
}
