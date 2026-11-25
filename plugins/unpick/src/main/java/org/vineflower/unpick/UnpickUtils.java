package org.vineflower.unpick;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassWriter;
import org.jetbrains.java.decompiler.main.DecompilerContext;
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

    static void processSuperMethods(StructClass cl, StructMethod mt, Consumer<StructMethod> processor) {
      String methodKey = InterpreterUtil.makeUniqueKey(mt.getName(), mt.getDescriptor());
      processSuperMethods(cl, methodKey, processor, new HashSet<>());
    }

    // TODO: handle bridges?
    static void processSuperMethods(StructClass cl, String methodKey, Consumer<StructMethod> processor, Set<String> visitedClasses) {
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
}
