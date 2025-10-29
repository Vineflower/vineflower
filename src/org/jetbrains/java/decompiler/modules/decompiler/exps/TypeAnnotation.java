// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;
import org.jetbrains.java.decompiler.util.Pair;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.*;
import java.util.function.Predicate;

public class TypeAnnotation {
  public static final int CLASS_TYPE_PARAMETER = 0x00;
  public static final int METHOD_TYPE_PARAMETER = 0x01;
  public static final int SUPER_TYPE_REFERENCE = 0x10;
  public static final int CLASS_TYPE_PARAMETER_BOUND = 0x11;
  public static final int METHOD_TYPE_PARAMETER_BOUND = 0x12;
  public static final int FIELD = 0x13;
  public static final int METHOD_RETURN_TYPE = 0x14;
  public static final int METHOD_RECEIVER = 0x15;
  public static final int METHOD_PARAMETER = 0x16;
  public static final int THROWS_REFERENCE = 0x17;
  public static final int LOCAL_VARIABLE = 0x40;
  public static final int RESOURCE_VARIABLE = 0x41;
  public static final int CATCH_CLAUSE = 0x42;
  public static final int EXPR_INSTANCEOF = 0x43;
  public static final int EXPR_NEW = 0x44;
  public static final int EXPR_CONSTRUCTOR_REF = 0x45;
  public static final int EXPR_METHOD_REF = 0x46;
  public static final int TYPE_ARG_CAST = 0x47;
  public static final int TYPE_ARG_CONSTRUCTOR_CALL = 0x48;
  public static final int TYPE_ARG_METHOD_CALL = 0x49;
  public static final int TYPE_ARG_CONSTRUCTOR_REF = 0x4A;
  public static final int TYPE_ARG_METHOD_REF = 0x4B;

  private final int target;
  public final PathValue[] path;
  private final AnnotationExprent annotation;

  public enum PathKind {
    DEEPER_ARRAY,
    DEEPER_NESTED,
    ON_WILDCARD,
    GENERIC_ARGUMENT;

    public static final PathKind[] VALUES = values();
  }

  public record PathValue(PathKind kind, int typeArg) {
    public PathValue {
      if (kind != PathKind.GENERIC_ARGUMENT && typeArg != 0) {
        throw new IllegalStateException("Illegal path data");
      }
    }
  }

  public TypeAnnotation(int target, PathValue[] path, AnnotationExprent annotation) {
    this.target = target;
    this.path = path;
    this.annotation = annotation;
  }

  public int getTargetType() {
    return target >> 24;
  }

  public int getIndex() {
    return target & 0xFFFF;
  }

  public boolean isTopLevel() {
    return path.length == 0;
  }

  public Queue<PathValue> asQueue() {
    return new ArrayDeque<>(Arrays.asList(path));
  }

  public AnnotationExprent getAnnotation() {
    return annotation;
  }

  private static boolean isStaticInContext(ClassesProcessor.ClassNode node, ClassesProcessor.ClassNode currentNode, ClassesProcessor.ClassNode outermostNode) {
    // Don't check for parents if there are no non-static members here
    boolean anyStatic = false;
    while (outermostNode != null) {
      if (outermostNode.hasModifier(CodeConstants.ACC_STATIC)) {
        anyStatic = true;
      }

      outermostNode = outermostNode.parent;
    }

    if (anyStatic) {
      // If there are any static classes, then we need to check for
      do {
        if (node == currentNode) {
          return true;
        }

        currentNode = currentNode.parent;
      } while (currentNode != null);
    }

    return node.hasModifier(CodeConstants.ACC_STATIC);
  }

  public static void appendCastTypeName(TextBuffer buffer, VarType type, List<Pair<Queue<TypeAnnotation.PathValue>, AnnotationExprent>> typeAnnotations, Set<String> filter, boolean vararg, boolean flush) {
    // Before going through the rest of the classes, go through the array. Save the values for later, as we'll need to append them.
    TextBuffer appendBuffer = new TextBuffer();
    if (type.arrayDim > 0) {
      for (int i = 0; i < type.arrayDim; i++) {
        stepwiseWriteAnnotations(appendBuffer, true, typeAnnotations, filter, v -> v.kind() == TypeAnnotation.PathKind.DEEPER_ARRAY);

        if (i == type.arrayDim - 1 && vararg && flush) { // flush == top level, only check for varargs there
          appendBuffer.append("...");
        } else {
          appendBuffer.append("[]");
        }
      }
    }

    if (type.type == CodeType.OBJECT) {
      ClassesProcessor.ClassNode currentNode = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS_NODE);
      // Unused getShortName is to make sure that all linked classes here will be properly added to the imports.
      DecompilerContext.getImportCollector().getShortName(ExprProcessor.buildJavaClassName(type.value));

      List<String> fullClasses = new ArrayList<>();
      String string = ExprProcessor.buildJavaClassName(type.value);
      int last$ = 0;

      // Find all the superclass inners
      while (string.substring(last$).indexOf('$') >= 0) {
        int idx = string.substring(last$).indexOf('$');
        if (idx < 0) {
          break;
        }

        last$ += idx + 1;
        fullClasses.add(string.substring(0, last$ - 1));
      }

      fullClasses.add(string);

      ClassesProcessor.ClassNode outermostNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(string.replace('.', '/'));

      // Find the starting point for the left->right traversal of inner classes, based on whether the current class is static in context or not
      int startIdx = 0;
      for (int i = 1; i < fullClasses.size(); i++) {
        String clazz = fullClasses.get(i);
        ClassesProcessor.ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(clazz.replace('.', '/'));
        if (node != null && isStaticInContext(node, currentNode, outermostNode)) {
          startIdx++;
        } else {
          break;
        }
      }

      int i = 0;
      for (String clazz : fullClasses) {
        ClassesProcessor.ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(clazz.replace('.', '/'));
        // Try to find the name, if the node exists use that, otherwise just use the descriptor
        String name;
        if (node != null) {
          name = node.simpleName;
        } else {
          int $ = clazz.indexOf("$");
          name = clazz.substring(clazz.lastIndexOf(".") + 1, $ > 0 ? $ : clazz.length());
        }

        if (i > 0) {
          buffer.append(".");
        }

        // If we're past the point of being allowed to place annotations, start unwinding the queue
        if (i >= startIdx) {
          stepwiseWriteAnnotations(buffer, typeAnnotations, filter, v -> v.kind() == TypeAnnotation.PathKind.DEEPER_NESTED);
        }

        buffer.appendClass(name, false, clazz);

        i++;
      }

      // Write generic parameters if the type is generic. Go through the arguments and recurse on the types there.
      if (type instanceof GenericType gt && !gt.getArguments().isEmpty()) {
        buffer.append("<");
        for (int j = 0; j < gt.getArguments().size(); j++) {
          if (j > 0) {
            buffer.append(", ");
          }
          VarType arg = gt.getArguments().get(j);

          // First, check if this is the argument that we care about
          int finalJ = j;
          stepwiseWriteAnnotations(buffer, typeAnnotations, filter, v -> v.kind() == TypeAnnotation.PathKind.GENERIC_ARGUMENT && v.typeArg() == finalJ);

          // Then, check if this wildcard is applicable for annotations
          if (arg == null || (arg instanceof GenericType genArg && genArg.getWildcard() != GenericType.WILDCARD_NO)) {
            stepwiseWriteAnnotations(buffer, typeAnnotations, filter, v -> v.kind() == TypeAnnotation.PathKind.ON_WILDCARD);
          }

          // Actually write the wildcard
          if (arg instanceof GenericType genArg) {
            if (genArg.getWildcard() == GenericType.WILDCARD_EXTENDS) {
              buffer.append("? extends ");
            } else if (genArg.getWildcard() == GenericType.WILDCARD_SUPER) {
              buffer.append("? super ");
            }
          } else if (arg == null) {
            buffer.append("?");
            continue;
          }

          // Recurse on the arg
          appendCastTypeName(buffer, arg, typeAnnotations, filter, vararg, false);
        }
        buffer.append(">");
      }

      // Append the array data
      buffer.append(appendBuffer);

      if (flush) {
        dumpUnwrittenAnnotations(buffer, typeAnnotations);
      }

      return;
    }

    // Non-class types, primitives and genvars
    // The only thing should be possible here is arrays (handled above) and top level annotations.

    stepwiseWriteAnnotations(buffer, typeAnnotations, filter, v -> false);

    buffer.appendCastTypeName(ExprProcessor.getCastTypeName(type), type);

    // Append the array data
    buffer.append(appendBuffer);

    // If we're unlucky, there's a bug in the implementation and we didn't write annotations properly. Dump them for debugging.
    if (flush) {
      dumpUnwrittenAnnotations(buffer, typeAnnotations);
    }
  }

  private static void stepwiseWriteAnnotations(TextBuffer buffer, List<Pair<Queue<TypeAnnotation.PathValue>, AnnotationExprent>> typeAnnotations, Set<String> filter, Predicate<TypeAnnotation.PathValue> canProceed) {
    stepwiseWriteAnnotations(buffer, false, typeAnnotations, filter, canProceed);
  }

  private static void stepwiseWriteAnnotations(TextBuffer buffer, boolean extraSpace, List<Pair<Queue<TypeAnnotation.PathValue>, AnnotationExprent>> typeAnnotations, Set<String> filter, Predicate<PathValue> canProceed) {
    for (Pair<Queue<TypeAnnotation.PathValue>, AnnotationExprent> anno : typeAnnotations) {
      if (anno.a.isEmpty()) {
        if (!anno.b.didWriteAlready()) {
          anno.b.setDidWriteAlready(true);

          TextBuffer annoJava = anno.b.toJava(-1);
          if (filter.contains(annoJava.convertToStringAndAllowDataDiscard())) {
            continue;
          }

          if (extraSpace) {
            buffer.append(" ");
          }

          buffer.append(annoJava);
          buffer.append(" ");
        }
      } else {
        if (canProceed.test(anno.a.peek())) {
          anno.a.poll();
        }
      }
    }
  }

  private static void dumpUnwrittenAnnotations(TextBuffer buffer, List<Pair<Queue<PathValue>, AnnotationExprent>> typeAnnotations) {
    boolean any = false;
    for (Pair<Queue<TypeAnnotation.PathValue>, AnnotationExprent> anno : typeAnnotations) {
      if (!anno.b.didWriteAlready()) {
        if (any) {
          buffer.append(" ");
        } else {
          buffer.append("/* ");
          any = true;
        }

        anno.b.setDidWriteAlready(true);
        buffer.append(anno.b.toJava(-1));
      }
    }

    if (any) {
      buffer.append(" */");
    }
  }
}