// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.vineflower.kotlin;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import org.jetbrains.java.decompiler.api.plugin.StatementWriter;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.Instruction;
import org.jetbrains.java.decompiler.code.InstructionSequence;
import org.jetbrains.java.decompiler.main.*;
import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.modules.renamer.PoolInterceptor;
import org.jetbrains.java.decompiler.struct.*;
import org.jetbrains.java.decompiler.struct.attr.*;
import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import org.jetbrains.java.decompiler.struct.consts.PooledConstant;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericClassDescriptor;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericFieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.util.*;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;
import org.vineflower.kotlin.expr.KAnnotationExprent;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.struct.*;
import org.vineflower.kotlin.util.KTypes;
import org.vineflower.kotlin.util.KUtils;
import org.vineflower.kotlin.util.ProtobufFlags;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class KotlinWriter implements StatementWriter {
  private static final Set<String> ERROR_DUMP_STOP_POINTS = new HashSet<>(Arrays.asList(
    "Fernflower.decompileContext",
    "MethodProcessor.codeToJava",
    "KotlinWriter.writeMethod"
  ));
  private static final String NOT_NULL_ANN_NAME = "org/jetbrains/annotations/NotNull";
  private static final String NULLABLE_ANN_NAME = "org/jetbrains/annotations/Nullable";
  private static final String REPEATABLE_ANN_NAME = "java/lang/annotation/Repeatable";
  private static final Set<String> KT_HARD_KEYWORDS = new HashSet<>(Arrays.asList(
    "as",
    "break",
    "class",
    "continue",
    "do",
    "else",
    "false",
    "for",
    "fun",
    "if",
    "in",
    "interface",
    "is",
    "null",
    "object",
    "package",
    "return",
    "super",
    "this",
    "throw",
    "true",
    "try",
    "typealias",
    "typeof", // Reserved for future use
    "val",
    "var",
    "when",
    "while"
  ));

  private final PoolInterceptor interceptor;
  private final IFabricJavadocProvider javadocProvider;

  public KotlinWriter() {
    interceptor = DecompilerContext.getPoolInterceptor();
    javadocProvider = (IFabricJavadocProvider) DecompilerContext.getProperty(IFabricJavadocProvider.PROPERTY_NAME);
  }

  private boolean invokeProcessors(TextBuffer buffer, ClassNode node) {
    ClassWrapper wrapper = node.getWrapper();
    if (wrapper == null) {
      buffer.append("/* $VF: Couldn't be decompiled. Class " + node.classStruct.qualifiedName + " wasn't processed yet! */");
      List<String> lines = new ArrayList<>();
      lines.addAll(KotlinWriter.getErrorComment());
      for (String line : lines) {
        buffer.append("//");
        if (!line.isEmpty()) buffer.append(' ').append(line);
        buffer.appendLineSeparator();
      }
      return false; // Doesn't make sense! how is this null? referencing an anonymous class in another object?
    }
    StructClass cl = wrapper.getClassStruct();

    try {
      InitializerProcessor.extractInitializers(wrapper);
      InitializerProcessor.hideInitalizers(wrapper);

      if (node.type == ClassNode.Type.ROOT &&
        cl.getVersion().has14ClassReferences() &&
        DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_CLASS_1_4)) {
        ClassReference14Processor.processClassReferences(node);
      }

      if (cl.hasModifier(CodeConstants.ACC_ENUM) && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM)) {
        EnumProcessor.clearEnum(wrapper);
      }

      if (DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ASSERTIONS)) {
        AssertProcessor.buildAssertions(node);
      }
    } catch (Throwable t) {
      DecompilerContext.getLogger().writeMessage("Class " + node.simpleName + " couldn't be written.",
        IFernflowerLogger.Severity.WARN,
        t);
      buffer.append("// $VF: Couldn't be decompiled");
      buffer.appendLineSeparator();
      if (DecompilerContext.getOption(IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR)) {
        List<String> lines = new ArrayList<>();
        lines.addAll(KotlinWriter.getErrorComment());
        collectErrorLines(t, lines);
        for (String line : lines) {
          buffer.append("//");
          if (!line.isEmpty()) buffer.append(' ').append(line);
          buffer.appendLineSeparator();
        }
      }

      return false;
    }

    return true;
  }

  public void writeClassHeader(StructClass cl, TextBuffer buffer, ImportCollector importCollector) {
    if (KotlinDecompilationContext.getCurrentType() == KotlinDecompilationContext.KotlinType.FILE) {
      for (Key<?> key : ANNOTATION_ATTRIBUTES) {
        StructAnnotationAttribute attr = cl.getAttribute((Key<StructAnnotationAttribute>) key);
        if (attr != null) {
          for (AnnotationExprent expr : attr.getAnnotations()) {
            if (expr.getClassName().equals("kotlin/Metadata")) {
              continue;
            }

            KAnnotationExprent kAnnotationExprent = new KAnnotationExprent(expr, KAnnotationExprent.UseSiteTarget.FILE);
            buffer.append(kAnnotationExprent.toJava(0)).appendLineSeparator().appendLineSeparator();
          }
        }
      }
    }

    int index = cl.qualifiedName.lastIndexOf('/');
    if (index >= 0) {
      buffer.append("package ");

      String[] packageParts = cl.qualifiedName.substring(0, index).split("/");
      for (int i = 0; i < packageParts.length; i++) {
        if (i > 0) buffer.append('.');
        buffer.append(toValidKotlinIdentifier(packageParts[i]));
      }

      buffer.appendLineSeparator().appendLineSeparator();
    }

    KotlinImportCollector kotlinImportCollector = new KotlinImportCollector(importCollector);
    kotlinImportCollector.writeImports(buffer, true);

    MetadataNameResolver nameResolver = KotlinDecompilationContext.getNameResolver();
    if (KotlinDecompilationContext.getCurrentType() == KotlinDecompilationContext.KotlinType.CLASS) {
      if (KotlinDecompilationContext.getCurrentClass().getTypeAliasCount() > 0) {
        List<ProtoBuf.TypeAlias> typeAliases = KotlinDecompilationContext.getCurrentClass().getTypeAliasList();
        for (ProtoBuf.TypeAlias typeAlias : typeAliases) {
          buffer.append("typealias ");
          buffer.append(toValidKotlinIdentifier(nameResolver.resolve(typeAlias.getName())));
          buffer.append(" = ");
          buffer.append(toValidKotlinIdentifier(nameResolver.resolve(typeAlias.getUnderlyingType().getClassName())));
          buffer.appendLineSeparator();
        }

        buffer.appendLineSeparator();
      }
    }
  }

  public void writeClass(ClassNode node, TextBuffer buffer, int indent) {
    ClassNode outerNode = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS_NODE);
    DecompilerContext.setProperty(DecompilerContext.CURRENT_CLASS_NODE, node);
    DecompilerContext.setImportCollector(new KotlinImportCollector(DecompilerContext.getImportCollector()));

    try {
      // last minute processing
      boolean ok = invokeProcessors(buffer, node);

      if (!ok) {
        return;
      }

      ClassWrapper wrapper = node.getWrapper();
      StructClass cl = wrapper.getClassStruct();
      ConstantPool pool = cl.getPool();

      KotlinChooser.setContextVariables(cl);

      DecompilerContext.getLogger().startWriteClass(cl.qualifiedName);

      KProperty.Data propertyData = KProperty.parse(node);
      Map<StructMethod, KFunction> functions = KFunction.parse(node);
      KConstructor.Data constructorData = KConstructor.parse(node);

      if (DecompilerContext.getOption(IFernflowerPreferences.SOURCE_FILE_COMMENTS)) {
        StructSourceFileAttribute sourceFileAttr = node.classStruct
          .getAttribute(StructGeneralAttribute.ATTRIBUTE_SOURCE_FILE);

        if (sourceFileAttr != null) {
          String sourceFile = sourceFileAttr.getSourceFile(pool);

          buffer
            .appendIndent(indent)
            .append("// $VF: Compiled from " + sourceFile)
            .appendLineSeparator();
        }
      }

      if (cl.hasModifier(CodeConstants.ACC_ANNOTATION)) {
        // Kotlin's annotation classes are treated quite differently from other classes
        writeAnnotationDefinition(node, buffer, indent, propertyData, functions, constructorData);
        return;
      }

      if (KotlinDecompilationContext.getCurrentType() == KotlinDecompilationContext.KotlinType.FILE) {
        writeKotlinFile(node, buffer, indent, propertyData, functions); // no constructors in top level file
        return;
      }

      // write class definition
      writeClassDefinition(node, buffer, indent, constructorData);

      boolean hasContent = false;
      boolean enumFields = false;

      List<StructRecordComponent> components = cl.getRecordComponents();

      // FIXME: fields don't have line mappings
      // fields

      // Find the last field marked as an enum
      int maxEnumIdx = 0;
      for (int i = 0; i < cl.getFields().size(); i++) {
        StructField fd = cl.getFields().get(i);
        boolean isEnum = fd.hasModifier(CodeConstants.ACC_ENUM) && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM);
        if (isEnum) {
          maxEnumIdx = i;
        }
      }

      List<StructField> deferredEnumFields = new ArrayList<>();

      // Find any regular fields mixed in with the enum fields
      // This is invalid but allowed in bytecode.
      for (int i = 0; i < cl.getFields().size(); i++) {
        StructField fd = cl.getFields().get(i);
        boolean isEnum = fd.hasModifier(CodeConstants.ACC_ENUM) && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM);
        if (i < maxEnumIdx && !isEnum) {
          deferredEnumFields.add(fd);
        }
      }

      for (StructField fd : cl.getFields()) {
        boolean hide = fd.isSynthetic() && DecompilerContext.getOption(IFernflowerPreferences.REMOVE_SYNTHETIC) ||
          wrapper.getHiddenMembers().contains(InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor())) || deferredEnumFields.contains(fd);
        if (hide) continue;

        if (components != null && fd.getAccessFlags() == (CodeConstants.ACC_FINAL | CodeConstants.ACC_PRIVATE) &&
          components.stream().anyMatch(c -> c.getName().equals(fd.getName()) && c.getDescriptor().equals(fd.getDescriptor()))) {
          // Record component field: skip it
          continue;
        }

        boolean isEnum = fd.hasModifier(CodeConstants.ACC_ENUM) && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM);
        if (isEnum) {
          if (enumFields) {
            buffer.append(',').appendLineSeparator();
          }
          enumFields = true;
        } else if (enumFields) {
          buffer.append(';');
          buffer.appendLineSeparator();
          buffer.appendLineSeparator();
          enumFields = false;

          // If the fields after are non enum, readd the fields found scattered throughout the enum
          for (StructField fd2 : deferredEnumFields) {
            TextBuffer fieldBuffer = new TextBuffer();
            writeField(wrapper, cl, fd2, fieldBuffer, indent + 1);
            fieldBuffer.clearUnassignedBytecodeMappingData();
            buffer.append(fieldBuffer);
          }
        }

        if (propertyData == null || !propertyData.associatedFields.contains(fd.getName())) {
          TextBuffer fieldBuffer = new TextBuffer();
          writeField(wrapper, cl, fd, fieldBuffer, indent + 1);
          fieldBuffer.clearUnassignedBytecodeMappingData();
          buffer.append(fieldBuffer);

          hasContent = true;
        }
      }

      if (enumFields) {
        buffer.append(';').appendLineSeparator();

        // If we end with enum fields, readd the fields found mixed in
        for (StructField fd2 : deferredEnumFields) {
          TextBuffer fieldBuffer = new TextBuffer();
          writeField(wrapper, cl, fd2, fieldBuffer, indent + 1);
          fieldBuffer.clearUnassignedBytecodeMappingData();
          buffer.append(fieldBuffer);
        }
      }

      if (propertyData != null && !propertyData.properties.isEmpty()) {
        if (hasContent) {
          buffer.appendLineSeparator();
        }

        for (KProperty prop : propertyData.properties) {
          buffer.append(prop.stringify(indent + 1));
        }

        hasContent = true;
      }

      // methods
      VBStyleCollection<StructMethod, String> methods = cl.getMethods();
      for (int i = 0; i < methods.size(); i++) {
        StructMethod mt = methods.get(i);
        boolean hide = mt.isSynthetic() && DecompilerContext.getOption(IFernflowerPreferences.REMOVE_SYNTHETIC) ||
          mt.hasModifier(CodeConstants.ACC_BRIDGE) && DecompilerContext.getOption(IFernflowerPreferences.REMOVE_BRIDGE) ||
          wrapper.getHiddenMembers().contains(InterpreterUtil.makeUniqueKey(mt.getName(), mt.getDescriptor())) ||
          mt.getName().equals("<init>") && mt.getDescriptor().equals("(Lkotlin/jvm/internal/DefaultConstructorMarker;)V") ||
          propertyData != null && propertyData.associatedMethods.contains(InterpreterUtil.makeUniqueKey(mt.getName(), mt.getDescriptor()));
        if (hide) continue;

        KFunction function = functions.get(mt);
        if (function != null) {
          if (hasContent) {
            buffer.appendLineSeparator();
          }
          hasContent = true;
          buffer.append(function.stringify(indent + 1));
          continue;
        }

        if (constructorData != null) {
          KConstructor constructor = constructorData.constructors.get(mt);
          if (constructor != null) {
            if (hasContent) {
              buffer.appendLineSeparator();
            }
            hasContent |= constructor.stringify(buffer, indent + 1);
            continue;
          }
        }

        TextBuffer methodBuffer = new TextBuffer();
        boolean methodSkipped = !writeMethod(node, mt, i, methodBuffer, indent + 1);
        if (!methodSkipped) {
          if (hasContent) {
            buffer.appendLineSeparator();
          }
          hasContent = true;
          buffer.append(methodBuffer);
        }
      }

      // member classes
      for (ClassNode inner : node.nested) {
        if (inner.type == ClassNode.Type.MEMBER) {
          StructClass innerCl = inner.classStruct;
          boolean isSynthetic = (inner.access & CodeConstants.ACC_SYNTHETIC) != 0 || innerCl.isSynthetic();
          boolean hide = isSynthetic && DecompilerContext.getOption(IFernflowerPreferences.REMOVE_SYNTHETIC) ||
            wrapper.getHiddenMembers().contains(innerCl.qualifiedName);
          if (hide) continue;

          if (hasContent) {
            buffer.appendLineSeparator();
          }
          writeClass(inner, buffer, indent + 1);

          hasContent = true;
        }
      }

      buffer.appendIndent(indent).append('}');

      if (node.type != ClassNode.Type.ANONYMOUS) {
        buffer.appendLineSeparator();
      }
    } finally {
      DecompilerContext.setProperty(DecompilerContext.CURRENT_CLASS_NODE, outerNode);
      DecompilerContext.getLogger().endWriteClass();
    }
  }

  private void writeKotlinFile(ClassNode node, TextBuffer buffer, int indent, KProperty.Data propertyData, Map<StructMethod, KFunction> functions) {
    ClassWrapper wrapper = node.getWrapper();
    StructClass cl = wrapper.getClassStruct();

    for (KProperty property : propertyData.properties) {
      buffer.append(property.stringify(indent));
    }

    for (StructField fd : cl.getFields()) {
      if (propertyData.associatedFields.contains(fd.getName())) continue;

      TextBuffer fieldBuffer = new TextBuffer();
      writeField(wrapper, cl, fd, fieldBuffer, indent);
      fieldBuffer.clearUnassignedBytecodeMappingData();
      buffer.append(fieldBuffer);
    }

    if (!cl.getFields().isEmpty()) {
      buffer.appendLineSeparator();
    }

    for (int i = 0; i < cl.getMethods().size(); i++) {
      StructMethod mt = cl.getMethods().get(i);
      if (functions.containsKey(mt)) {
        buffer.append(functions.get(mt).stringify(indent));
        buffer.appendLineSeparator();
        continue;
      }

      String key = InterpreterUtil.makeUniqueKey(mt.getName(), mt.getDescriptor());
      if (mt.getName().equals("<clinit>") || propertyData.associatedMethods.contains(key)) continue;

      TextBuffer methodBuffer = new TextBuffer();
      writeMethod(node, mt, i, methodBuffer, indent);
      methodBuffer.clearUnassignedBytecodeMappingData();
      buffer.append(methodBuffer, node.simpleName, mt.getName() + " " + mt.getDescriptor());
      buffer.appendLineSeparator();
    }

    for (ClassNode inner : node.nested) {
      if (inner.type == ClassNode.Type.MEMBER) {
        writeClass(inner, buffer, indent);
      }
    }
  }

  private void writeAnnotationDefinition(ClassNode node, TextBuffer buffer, int indent, KProperty.Data propertyData, Map<StructMethod, KFunction> functions, KConstructor.Data constructorData) {
    ClassWrapper wrapper = node.getWrapper();
    StructClass cl = wrapper.getClassStruct();

    StructAnnotationAttribute runtimeAnnotations = cl.getAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS);
    AnnotationExprent repeatableAnnotation = runtimeAnnotations.getAnnotations().stream()
      .filter(a -> REPEATABLE_ANN_NAME.equals(a.getClassName()))
      .findFirst()
      .orElse(null);

    String repeatableContainer = null;

    if (repeatableAnnotation != null) {
      repeatableContainer = ((ConstExprent) repeatableAnnotation.getParValues().get(0)).getValue().toString();
      runtimeAnnotations.getAnnotations().remove(repeatableAnnotation);
    }

    appendAnnotations(buffer, indent, cl, -1);
    appendJvmAnnotations(buffer, indent, cl, true, cl.getPool(), TypeAnnotation.CLASS_TYPE_PARAMETER);

    buffer.appendIndent(indent);
    appendModifiers(buffer, cl.getAccessFlags(), CLASS_ALLOWED, true, CLASS_EXCLUDED);

    VarType classType = new VarType(cl.qualifiedName, true);

    buffer.append("annotation class ")
      .append(KTypes.getKotlinType(classType, false))
      .append("(")
      .appendLineSeparator();

    List<KProperty> nonParameterProperties = new ArrayList<>(propertyData.properties);

    boolean first = true;
    for (KParameter param : constructorData.primary.parameters) {
      if (!first) {
        buffer.append(",").appendLineSeparator();
      }
      first = false;
      buffer.appendIndent(indent + 1)
        .append("val ")
        .append(KotlinWriter.toValidKotlinIdentifier(param.name))
        .append(": ")
        .append(param.type.stringify(indent + 1));

      // Because Kotlin really doesn't like making this easy for us, defaults are still passed directly via attributes
      KProperty prop = propertyData.properties.stream()
        .filter(p -> p.name.equals(param.name))
        .findFirst()
        .orElseThrow();

      nonParameterProperties.remove(prop);

      KPropertyAccessor getter = prop.getter;
      if (getter != null) {
        StructMethod mt = getter.underlyingMethod.methodStruct;
        StructAnnDefaultAttribute paramAttr = mt.getAttribute(StructGeneralAttribute.ATTRIBUTE_ANNOTATION_DEFAULT);
        if (paramAttr != null) {
          Exprent kExpr = KUtils.replaceExprent(paramAttr.getDefaultValue());
          Exprent expr = kExpr != null ? kExpr : paramAttr.getDefaultValue();
          buffer.append(" = ").append(expr.toJava());
        }
      }
    }

    buffer.appendLineSeparator()
      .appendIndent(indent)
      .append(")");

    boolean appended = false;

    TextBuffer innerBuffer = new TextBuffer();

    for (KProperty prop : nonParameterProperties) {
      innerBuffer.append(prop.stringify(indent + 1));
      appended = true;
    }

    first = true;
    for (KFunction function : functions.values()) {
      if (!first || appended) {
        innerBuffer.appendLineSeparator();
      }
      first = false;

      innerBuffer.append(function.stringify(indent + 1));
      appended = true;
    }

    first = true;
    for (ClassNode inner : node.nested) {
      if (inner.type == ClassNode.Type.MEMBER) {
        if (inner.classStruct.qualifiedName.equals(repeatableContainer)) {
          // Skip the container class
          continue;
        }

        if (!first || appended) {
          innerBuffer.appendLineSeparator();
        }
        first = false;

        writeClass(inner, innerBuffer, indent + 1);
        appended = true;
      }
    }

    if (appended) {
      buffer.append(" {")
        .appendLineSeparator()
        .append(innerBuffer)
        .appendIndent(indent)
        .append("}");
    }

    buffer.appendLineSeparator();
  }

  private static boolean isGenerated(int flags) {
    return (flags & (CodeConstants.ACC_SYNTHETIC | CodeConstants.ACC_MANDATED)) != 0;
  }

  private void writeClassDefinition(ClassNode node, TextBuffer buffer, int indent, KConstructor.Data constructorData) {
    if (node.type == ClassNode.Type.ANONYMOUS) {
      buffer.append(" {").appendLineSeparator();
      return;
    }

    ClassWrapper wrapper = node.getWrapper();
    StructClass cl = wrapper.getClassStruct();

    int flags = node.type == ClassNode.Type.ROOT ? cl.getAccessFlags() : node.access;
    boolean isDeprecated = cl.hasAttribute(StructGeneralAttribute.ATTRIBUTE_DEPRECATED);
    boolean isSynthetic = (flags & CodeConstants.ACC_SYNTHETIC) != 0 || cl.hasAttribute(StructGeneralAttribute.ATTRIBUTE_SYNTHETIC);
    boolean isEnum = DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM) && (flags & CodeConstants.ACC_ENUM) != 0;
    boolean isInterface = (flags & CodeConstants.ACC_INTERFACE) != 0;
    boolean isAnnotation = (flags & CodeConstants.ACC_ANNOTATION) != 0;
    boolean isModuleInfo = (flags & CodeConstants.ACC_MODULE) != 0 && cl.hasAttribute(StructGeneralAttribute.ATTRIBUTE_MODULE);
    StructPermittedSubclassesAttribute permittedSubClassesAttr = cl.getAttribute(StructGeneralAttribute.ATTRIBUTE_PERMITTED_SUBCLASSES);
    List<String> permittedSubClasses = permittedSubClassesAttr != null ? permittedSubClassesAttr.getClasses() : Collections.emptyList();
    boolean isSealed = permittedSubClassesAttr != null && !permittedSubClasses.isEmpty();
    boolean isFinal = (flags & CodeConstants.ACC_FINAL) != 0;
    boolean isNonSealed = !isSealed && !isFinal && cl.getVersion().hasSealedClasses() && isSuperClassSealed(cl);

    if (isDeprecated) {
      if (!containsDeprecatedAnnotation(cl)) {
        appendDeprecation(buffer, indent);
      }
    }

    if (interceptor != null) {
      String oldName = interceptor.getOldName(cl.qualifiedName);
      appendRenameComment(buffer, oldName, MType.CLASS, indent);
    }

    if (javadocProvider != null) {
      appendJavadoc(buffer, javadocProvider.getClassDoc(cl), indent);
    }

    appendAnnotations(buffer, indent, cl, -1);
    appendJvmAnnotations(buffer, indent, cl, isInterface, cl.getPool(), TypeAnnotation.CLASS_TYPE_PARAMETER);


    ProtoBuf.Class proto = KotlinDecompilationContext.getCurrentClass();
    ProtobufFlags.Class kotlinFlags;
    if (proto != null) {
      kotlinFlags = new ProtobufFlags.Class(proto.getFlags());
    } else {
      appendComment(buffer, "Class flags could not be determined", indent);
      kotlinFlags = new ProtobufFlags.Class(0);
    }

    buffer.appendIndent(indent);

    if (kotlinFlags.visibility != ProtoBuf.Visibility.PUBLIC) {
      buffer.append(ProtobufFlags.toString(kotlinFlags.visibility)).append(' ');
    }

    if (kotlinFlags.isExpect) {
      buffer.append("expect ");
    }

    if ((!isInterface && kotlinFlags.modality != ProtoBuf.Modality.FINAL) || kotlinFlags.modality == ProtoBuf.Modality.SEALED) {
      buffer.append(ProtobufFlags.toString(kotlinFlags.modality)).append(' ');
    }

    if (kotlinFlags.isExternal) {
      buffer.append("external ");
    }
    if (kotlinFlags.isInner) {
      buffer.append("inner ");
    }
    if (kotlinFlags.isFun) {
      buffer.append("fun ");
    }
    if (kotlinFlags.isInline) {
      buffer.append("inline ");
    }
    if (kotlinFlags.isData) {
      buffer.append("data ");
    }

    if (isEnum) {
      buffer.append("enum class ");
    } else if (isInterface) {
      buffer.append("interface ");
    } else if (isAnnotation) {
      buffer.append("annotation class");
    } else if (kotlinFlags.kind == ProtoBuf.Class.Kind.OBJECT) {
      buffer.append("object ");
    } else if (kotlinFlags.kind == ProtoBuf.Class.Kind.COMPANION_OBJECT) {
      buffer.append("companion object ");
    } else {
      buffer.append("class ");
    }

    buffer.append(toValidKotlinIdentifier(node.simpleName));

    GenericClassDescriptor descriptor = cl.getSignature();
    if (descriptor != null && !descriptor.fparameters.isEmpty()) {
      appendTypeParameters(buffer, descriptor.fparameters, descriptor.fbounds);
    }

    buffer.pushNewlineGroup(indent, 1);

    boolean appendedColon = false;
    if (!isEnum && !isInterface && cl.superClass != null) {
      if (constructorData != null && constructorData.primary != null && constructorData.primary.writePrimaryConstructor(buffer, indent)) {
        appendedColon = true;
      } else {
        VarType supertype = new VarType(cl.superClass.getString(), true);
        if (!VarType.VARTYPE_OBJECT.equals(supertype)) {
          buffer.appendPossibleNewline(" ");
          buffer.append(": ");
          buffer.append(ExprProcessor.getCastTypeName(descriptor == null ? supertype : descriptor.superclass));
          appendedColon = true;
        }
      }
    }

    if (!isAnnotation) {
      int[] interfaces = cl.getInterfaces();
      if (interfaces.length > 0) {
        for (int i = 0; i < interfaces.length; i++) {
          if (!appendedColon) {
            buffer.append(" :");
            appendedColon = true;
          } else {
            buffer.append(",");
          }
          buffer.appendPossibleNewline(" ");

          if (descriptor == null || descriptor.superinterfaces.size() > i) {
            buffer.append(ExprProcessor.getCastTypeName(descriptor == null ? new VarType(cl.getInterface(i), true) : descriptor.superinterfaces.get(i)));
          }
        }
      }
    }

    buffer.popNewlineGroup();

    buffer.append(" {").appendLineSeparator();
  }

  private static boolean isSuperClassSealed(StructClass cl) {
    if (cl.superClass != null) {
      StructClass superClass = DecompilerContext.getStructContext().getClass((String) cl.superClass.value);
      if (superClass != null && superClass.hasAttribute(StructGeneralAttribute.ATTRIBUTE_PERMITTED_SUBCLASSES)) {
        return true;
      }
    }
    for (String iface : cl.getInterfaceNames()) {
      StructClass ifaceClass = DecompilerContext.getStructContext().getClass(iface);
      if (ifaceClass != null && ifaceClass.hasAttribute(StructGeneralAttribute.ATTRIBUTE_PERMITTED_SUBCLASSES)) {
        return true;
      }
    }
    return false;
  }

  public void writeField(ClassWrapper wrapper, StructClass cl, StructField fd, TextBuffer buffer, int indent) {
    boolean isInterface = cl.hasModifier(CodeConstants.ACC_INTERFACE);
    boolean isDeprecated = fd.hasAttribute(StructGeneralAttribute.ATTRIBUTE_DEPRECATED);
    boolean isEnum = fd.hasModifier(CodeConstants.ACC_ENUM) && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM);

    if (isDeprecated) {
      if (!containsDeprecatedAnnotation(fd)) {
        appendDeprecation(buffer, indent);
      }
    }

    String name = fd.getName();
    if (interceptor != null) {
      String newName = interceptor.getName(cl.qualifiedName + " " + fd.getName() + " " + fd.getDescriptor());

      if (newName != null) {
        name = newName.split(" ")[1];
      }
    }

    if (interceptor != null) {
      String oldName = interceptor.getOldName(cl.qualifiedName + " " + name + " " + fd.getDescriptor());
      appendRenameComment(buffer, oldName, MType.FIELD, indent);
    }

    if (javadocProvider != null) {
      appendJavadoc(buffer, javadocProvider.getFieldDoc(cl, fd), indent);
    }
    appendAnnotations(buffer, indent, fd, TypeAnnotation.FIELD);
    appendJvmAnnotations(buffer, indent, fd, isInterface, cl.getPool(), TypeAnnotation.FIELD);

    buffer.appendIndent(indent);

    if (!fd.hasModifier(CodeConstants.ACC_FINAL) && !fd.hasModifier(CodeConstants.ACC_STATIC) && !fd.hasModifier(CodeConstants.ACC_PRIVATE)) {
      buffer.append("open ");
    }

    if (!isEnum) {
      appendModifiers(buffer, fd.getAccessFlags(), FIELD_ALLOWED, isInterface, FIELD_EXCLUDED);
    }

    Map.Entry<VarType, GenericFieldDescriptor> fieldTypeData = getFieldTypeData(fd);
    VarType fieldType = fieldTypeData.getKey();
    GenericFieldDescriptor descriptor = fieldTypeData.getValue();

    if (!isEnum) {
      buffer.append(ExprProcessor.getCastTypeName(descriptor == null ? fieldType : descriptor.type));
      buffer.append(' ');
    }

    buffer.append(name);

    Exprent initializer;
    if (fd.hasModifier(CodeConstants.ACC_STATIC)) {
      initializer = wrapper.getStaticFieldInitializers().getWithKey(InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor()));
    } else {
      initializer = wrapper.getDynamicFieldInitializers().getWithKey(InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor()));
    }

    if (initializer != null) {
      if (isEnum && initializer instanceof NewExprent) {
        NewExprent expr = (NewExprent) initializer;
        expr.setEnumConst(true);
        buffer.append(expr.toJava(indent));
      } else {
        buffer.append(" = ");

        if (initializer instanceof ConstExprent) {
          ((ConstExprent) initializer).adjustConstType(fieldType);
        }

        // FIXME: special case field initializer. Can map to more than one method (constructor) and bytecode instruction.
        ExprProcessor.getCastedExprent(initializer, descriptor == null ? fieldType : descriptor.type, buffer, indent, false);
      }
    } else if (fd.hasModifier(CodeConstants.ACC_FINAL) && fd.hasModifier(CodeConstants.ACC_STATIC)) {
      StructConstantValueAttribute attr = fd.getAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE);
      if (attr != null) {
        PrimitiveConstant constant = cl.getPool().getPrimitiveConstant(attr.getIndex());
        buffer.append(" = ");
        buffer.append(new ConstExprent(fieldType, constant.value, null).toJava(indent));
      }
    }

    if (!isEnum) {
      buffer.append(";").appendLineSeparator();
    }
  }

  public static String toValidKotlinIdentifier(String name) {
    if (name == null || name.isEmpty()) return name;

    if (KT_HARD_KEYWORDS.contains(name)) {
      return "`" + name + "`";
    }

    boolean requiresBackticks = !Character.isJavaIdentifierStart(name.charAt(0)) || name.charAt(0) == '$';
    for (int i = 1; i < name.length(); i++) {
      if (!Character.isJavaIdentifierPart(name.charAt(i)) || name.charAt(i) == '$') {
        requiresBackticks = true;
        break;
      }
    }
    boolean needsComment = false;
    if (name.contains("`")) {
      name = name.replace("`", "_");
      needsComment = true;
    }

    if (requiresBackticks) {
      name = "`" + name + "`";
    }
    return name + (needsComment ? " /* $VF was: " + name + " */" : "");
  }

  public boolean writeMethod(ClassNode node, StructMethod mt, int methodIndex, TextBuffer buffer, int indent) {
    ClassWrapper wrapper = node.getWrapper();
    StructClass cl = wrapper.getClassStruct();
    // Get method by index, this keeps duplicate methods (with the same key) separate
    MethodWrapper methodWrapper = wrapper.getMethodWrapper(methodIndex);

    boolean hideMethod = false;

    MethodWrapper outerWrapper = (MethodWrapper) DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
    DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, methodWrapper);

    try {
      boolean isInterface = cl.hasModifier(CodeConstants.ACC_INTERFACE);
      boolean isAnnotation = cl.hasModifier(CodeConstants.ACC_ANNOTATION);
      boolean isEnum = cl.hasModifier(CodeConstants.ACC_ENUM) && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM);
      boolean isDeprecated = mt.hasAttribute(StructGeneralAttribute.ATTRIBUTE_DEPRECATED);
      boolean clInit = false, init = false, dInit = false;

      MethodDescriptor md = MethodDescriptor.parseDescriptor(mt, node);

      int flags = mt.getAccessFlags();
      if ((flags & CodeConstants.ACC_NATIVE) != 0) {
        flags &= ~CodeConstants.ACC_STRICT; // compiler bug: a strictfp class sets all methods to strictfp
      }
      if (CodeConstants.CLINIT_NAME.equals(mt.getName())) {
        flags &= CodeConstants.ACC_STATIC; // ignore all modifiers except 'static' in a static initializer
      }

      if (isDeprecated) {
        if (!containsDeprecatedAnnotation(mt)) {
          appendDeprecation(buffer, indent);
        }
      }

      String name = mt.getName();
      if (interceptor != null) {
        String newName = interceptor.getName(cl.qualifiedName + " " + mt.getName() + " " + mt.getDescriptor());

        if (newName != null) {
          name = newName.split(" ")[1];
        }
      }

      if (interceptor != null) {
        String oldName = interceptor.getOldName(cl.qualifiedName + " " + name + " " + mt.getDescriptor());
        appendRenameComment(buffer, oldName, MType.METHOD, indent);
      }

      boolean isBridge = (flags & CodeConstants.ACC_BRIDGE) != 0;
      if (isBridge) {
        appendComment(buffer, "bridge method", indent);
      }

      if (DecompilerContext.getOption(IFernflowerPreferences.DECOMPILER_COMMENTS) && methodWrapper.addErrorComment || methodWrapper.commentLines != null) {
        if (methodWrapper.addErrorComment) {
          for (String s : KotlinWriter.getErrorComment()) {
            methodWrapper.addComment(s);
          }
        }

        for (String s : methodWrapper.commentLines) {
          buffer.appendIndent(indent).append("// " + s).appendLineSeparator();
        }
      }

      if (javadocProvider != null) {
        appendJavadoc(buffer, javadocProvider.getMethodDoc(cl, mt), indent);
      }

      appendAnnotations(buffer, indent, mt, TypeAnnotation.METHOD_RETURN_TYPE);

      appendJvmAnnotations(buffer, indent, mt, isInterface, cl.getPool(), TypeAnnotation.METHOD_RETURN_TYPE);

      buffer.appendIndent(indent);

      if (CodeConstants.INIT_NAME.equals(name)) {
        if (node.type == ClassNode.Type.ANONYMOUS) {
          name = "";
          dInit = true;
        } else {
          name = node.simpleName;
          init = true;
        }
      } else if (CodeConstants.CLINIT_NAME.equals(name)) {
        name = "";
        clInit = true;
      }

      boolean didOverride = false;
      if (!CodeConstants.INIT_NAME.equals(mt.getName()) && !CodeConstants.CLINIT_NAME.equals(mt.getName()) && !mt.hasModifier(CodeConstants.ACC_STATIC) && !mt.hasModifier(CodeConstants.ACC_PRIVATE)) {
        // Search superclasses for methods that match the name and descriptor of this one.
        // Make sure not to search the current class otherwise it will return the current method itself!
        // TODO: record overrides
        boolean isOverride = searchForMethod(cl, mt.getName(), md, false);
        if (isOverride) {
          buffer.append("override ");
          didOverride = true;
          if (mt.hasModifier(CodeConstants.ACC_ABSTRACT)) {
            buffer.append("abstract ");
          }
          if (mt.hasModifier(CodeConstants.ACC_FINAL)) {
            buffer.append("final ");
          }
        }
      }

      if (!didOverride && !mt.hasModifier(CodeConstants.ACC_FINAL) && !mt.hasModifier(CodeConstants.ACC_PRIVATE) && !mt.hasModifier(CodeConstants.ACC_STATIC) && !isInterface && !isAnnotation && !isEnum && !cl.hasModifier(CodeConstants.ACC_FINAL)) {
        buffer.append(mt.hasModifier(CodeConstants.ACC_ABSTRACT) ? "abstract " : "open ");
      }

      if (!dInit) {
        buffer.append("fun ");
      }

      GenericMethodDescriptor descriptor = mt.getSignature();
      boolean throwsExceptions = false;
      int paramCount = 0;

      if (!clInit && !dInit) {
        boolean thisVar = !mt.hasModifier(CodeConstants.ACC_STATIC);

        int index = isEnum && init ? 3 : thisVar ? 1 : 0;
        int start = isEnum && init ? 2 : 0;

        if (descriptor != null && !descriptor.typeParameters.isEmpty()) {
          appendTypeParameters(buffer, descriptor.typeParameters, descriptor.typeParameterBounds);
          buffer.append(' ');
        }

        // TODO: More robust checks for extension functions
        String varprocName = methodWrapper.varproc.getVarName(new VarVersionPair(index, 0));
        boolean extension = varprocName != null && varprocName.startsWith("$this$");

        if (extension) {
          VarType paramType = descriptor != null && descriptor.parameterTypes.size() > 0 ? descriptor.parameterTypes.get(0) : md.params[0];
          String typeName = KTypes.getKotlinType(paramType);
          boolean isNullable = processParameterAnnotations(buffer, mt, 0);

          if (KTypes.isFunctionType(paramType)) {
            // Extension functions always need parentheses for function types as the receiver
            typeName = "(" + typeName + ")";
          }

          buffer.append(typeName);

          if (isNullable) {
            buffer.append('?');
          }

          buffer.append(".");

          paramCount++;
          start++;
          index += paramType.stackSize;
        }

        buffer.append(toValidKotlinIdentifier(name));
        buffer.append('(');

        List<VarVersionPair> mask = methodWrapper.synthParameters;

        int lastVisibleParameterIndex = -1;
        for (int i = 0; i < md.params.length; i++) {
          if (mask == null || mask.get(i) == null) {
            lastVisibleParameterIndex = i;
          }
        }
        if (lastVisibleParameterIndex != -1) {
          buffer.pushNewlineGroup(indent, 1);
          buffer.appendPossibleNewline();
        }

        List<StructMethodParametersAttribute.Entry> methodParameters = null;
        if (DecompilerContext.getOption(IFernflowerPreferences.USE_METHOD_PARAMETERS)) {
          StructMethodParametersAttribute attr = mt.getAttribute(StructGeneralAttribute.ATTRIBUTE_METHOD_PARAMETERS);
          if (attr != null) {
            methodParameters = attr.getEntries();
          }
        }

        boolean hasDescriptor = descriptor != null;
        //mask should now have the Outer.this in it... so this *shouldn't* be nessasary.
        //if (init && !isEnum && ((node.access & CodeConstants.ACC_STATIC) == 0) && node.type == ClassNode.CLASS_MEMBER)
        //    index++;

        boolean first = true;
        buffer.pushNewlineGroup(indent, 0);
        for (int i = start; i < md.params.length; i++) {
          VarType parameterType = hasDescriptor && paramCount < descriptor.parameterTypes.size() ? descriptor.parameterTypes.get(paramCount) : md.params[i];
          if (mask == null || mask.get(i) == null) {
            if (!first) {
              buffer.append(",");
              buffer.appendPossibleNewline(" ");
            }
            first = false;

            // @PAnn vararg? pName: pTy
            boolean nullable = processParameterAnnotations(buffer, mt, paramCount);

            boolean isVarArg = i == lastVisibleParameterIndex && mt.hasModifier(CodeConstants.ACC_VARARGS) && parameterType.arrayDim > 0;
            if (isVarArg) {
              buffer.append("vararg ");
            }

            String parameterName;
            if (methodParameters != null && i < methodParameters.size()) {
              parameterName = methodParameters.get(i).myName;
            } else {
              parameterName = methodWrapper.varproc.getVarName(new VarVersionPair(index, 0));
            }

            if ((flags & (CodeConstants.ACC_ABSTRACT | CodeConstants.ACC_NATIVE)) != 0) {
              String newParameterName = methodWrapper.methodStruct.getVariableNamer().renameAbstractParameter(parameterName, index);
              parameterName = !newParameterName.equals(parameterName) ? newParameterName : DecompilerContext.getStructContext().renameAbstractParameter(methodWrapper.methodStruct.getClassQualifiedName(), mt.getName(), mt.getDescriptor(), index - (((flags & CodeConstants.ACC_STATIC) == 0) ? 1 : 0), parameterName);

            }

            parameterName = toValidKotlinIdentifier(parameterName);

            buffer.append(parameterName == null ? "param" + index : parameterName); // null iff decompiled with errors
            buffer.append(": ");

            if (methodParameters != null && i < methodParameters.size()) {
              appendModifiers(buffer, methodParameters.get(i).myAccessFlags, CodeConstants.ACC_FINAL, isInterface, 0);
            } else if (methodWrapper.varproc.getVarFinal(new VarVersionPair(index, 0)) == VarTypeProcessor.FinalType.EXPLICIT_FINAL) {
              buffer.append("final ");
            }

            String typeName;
            if (isVarArg) {
              parameterType = parameterType.decreaseArrayDim();
            }
            typeName = KTypes.getKotlinType(parameterType);

            if (KTypes.isFunctionType(parameterType) && nullable) {
              typeName = "(" + typeName + ")";
            }

            buffer.append(typeName);

            if (nullable) {
              buffer.append("?");
            }

            paramCount++;
          }

          index += parameterType.stackSize;
        }
        buffer.popNewlineGroup();

        if (lastVisibleParameterIndex != -1) {
          buffer.appendPossibleNewline("", true);
          buffer.popNewlineGroup();
        }
        buffer.append(')');

        VarType retType = descriptor == null ? md.ret : descriptor.returnType;
        if (!init && !retType.isSuperset(VarType.VARTYPE_VOID)) {
          buffer.append(": ");
          String ret = KTypes.getKotlinType(retType);
          if (KTypes.isFunctionType(retType) && isNullable(mt)) {
            ret = "(" + ret + ")";
          }
          buffer.append(ret);
          if (isNullable(mt)) {
            buffer.append("?");
          }
        }
      }

      if ((flags & (CodeConstants.ACC_ABSTRACT | CodeConstants.ACC_NATIVE)) != 0) { // native or abstract method (explicit or interface)
        buffer.appendLineSeparator();
      } else {
        if (!clInit && !dInit) {
          buffer.append(' ');
        }

        boolean hideIfInit = (clInit || dInit || hideConstructor(node, init, throwsExceptions, paramCount, flags));

        hideMethod = !writeMethodBody(node, methodWrapper, buffer, indent, hideIfInit);
      }
    } finally {
      DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, outerWrapper);
    }

    // save total lines
    // TODO: optimize
    //tracer.setCurrentSourceLine(buffer.countLines(start_index_method));

    return !hideMethod;
  }

  public static boolean writeMethodBody(ClassNode node, MethodWrapper methodWrapper, TextBuffer buffer, int indent, boolean hideIfInit) {
    boolean hideMethod = true;
    StructClass cl = node.classStruct;
    StructMethod mt = methodWrapper.methodStruct;

    buffer.append('{').appendLineSeparator();

    RootStatement root = methodWrapper.root;

    if (root != null && methodWrapper.decompileError == null) { // check for existence
      try {
        // Avoid generating imports for ObjectMethods during root.toJava(...)
        if (RecordHelper.isHiddenRecordMethod(cl, mt, root)) {
          hideMethod = true;
        } else {
          TextBuffer code = root.toJava(indent + 1);
          code.addBytecodeMapping(root.getDummyExit().bytecode);
          hideMethod = code.length() == 0 && hideIfInit;
          buffer.append(code, cl.qualifiedName, InterpreterUtil.makeUniqueKey(mt.getName(), mt.getDescriptor()));
        }
      } catch (Throwable t) {
        String message = "Method " + mt.getName() + " " + mt.getDescriptor() + " in class " + node.classStruct.qualifiedName + " couldn't be written.";
        DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN, t);
        methodWrapper.decompileError = t;
      }
    }

    if (methodWrapper.decompileError != null) {
      dumpError(buffer, methodWrapper, indent + 1);
    }
    buffer.appendIndent(indent).append('}').appendLineSeparator();
    return !hideMethod;
  }

  public static void dumpError(TextBuffer buffer, MethodWrapper wrapper, int indent) {
    List<String> lines = new ArrayList<>();
    lines.add("$VF: Couldn't be decompiled");
    boolean exceptions = DecompilerContext.getOption(IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR);
    boolean bytecode = DecompilerContext.getOption(IFernflowerPreferences.DUMP_BYTECODE_ON_ERROR);
    if (exceptions) {
      lines.addAll(KotlinWriter.getErrorComment());
      collectErrorLines(wrapper.decompileError, lines);
      if (bytecode) {
        lines.add("");
      }
    }
    if (bytecode) {
      try {
        lines.add("Bytecode:");
        collectBytecode(wrapper, lines);
      } catch (Exception e) {
        lines.add("Error collecting bytecode:");
        collectErrorLines(e, lines);
      } finally {
        wrapper.methodStruct.releaseResources();
      }
    }
    for (String line : lines) {
      buffer.appendIndent(indent);
      buffer.append("//");
      if (!line.isEmpty()) buffer.append(' ').append(line);
      buffer.appendLineSeparator();
    }
  }

  public static void collectErrorLines(Throwable error, List<String> lines) {
    StackTraceElement[] stack = error.getStackTrace();
    List<StackTraceElement> filteredStack = new ArrayList<>();
    boolean hasSeenOwnClass = false;
    for (StackTraceElement e : stack) {
      String className = e.getClassName();
      boolean isOwnClass = className.startsWith("org.jetbrains.java.decompiler");
      if (isOwnClass) {
        hasSeenOwnClass = true;
      } else if (hasSeenOwnClass) {
        break;
      }
      filteredStack.add(e);
      if (isOwnClass) {
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        if (ERROR_DUMP_STOP_POINTS.contains(simpleName + "." + e.getMethodName())) {
          break;
        }
      }
    }
    if (filteredStack.isEmpty()) return;
    lines.add(error.toString());
    for (StackTraceElement e : filteredStack) {
      lines.add("  at " + e);
    }
    Throwable cause = error.getCause();
    if (cause != null) {
      List<String> causeLines = new ArrayList<>();
      collectErrorLines(cause, causeLines);
      if (!causeLines.isEmpty()) {
        lines.add("Caused by: " + causeLines.get(0));
        lines.addAll(causeLines.subList(1, causeLines.size()));
      }
    }
  }

  private static void collectBytecode(MethodWrapper wrapper, List<String> lines) throws IOException {
    ClassNode classNode = (ClassNode) DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS_NODE);
    StructMethod method = wrapper.methodStruct;
    InstructionSequence instructions = method.getInstructionSequence();
    if (instructions == null) {
      method.expandData(classNode.classStruct);
      instructions = method.getInstructionSequence();
    }
    int lastOffset = instructions.getOffset(instructions.length() - 1);
    int digits = 8 - Integer.numberOfLeadingZeros(lastOffset) / 4;
    ConstantPool pool = classNode.classStruct.getPool();
    StructBootstrapMethodsAttribute bootstrap = classNode.classStruct.getAttribute(StructGeneralAttribute.ATTRIBUTE_BOOTSTRAP_METHODS);

    for (int idx = 0; idx < instructions.length(); idx++) {
      int offset = instructions.getOffset(idx);
      Instruction instr = instructions.getInstr(idx);
      StringBuilder sb = new StringBuilder();
      String offHex = Integer.toHexString(offset);
      for (int i = offHex.length(); i < digits; i++) sb.append('0');
      sb.append(offHex).append(": ");
      if (instr.wide) {
        sb.append("wide ");
      }
      sb.append(TextUtil.getInstructionName(instr.opcode));
      switch (instr.group) {
        case CodeConstants.GROUP_INVOCATION: {
          sb.append(' ');
          if (instr.opcode == CodeConstants.opc_invokedynamic && bootstrap != null) {
            appendBootstrapCall(sb, pool.getLinkConstant(instr.operand(0)), bootstrap);
          } else {
            appendConstant(sb, pool.getConstant(instr.operand(0)));
          }
          for (int i = 1; i < instr.operandsCount(); i++) {
            sb.append(' ').append(instr.operand(i));
          }
          break;
        }
        case CodeConstants.GROUP_FIELDACCESS: {
          sb.append(' ');
          appendConstant(sb, pool.getConstant(instr.operand(0)));
          break;
        }
        case CodeConstants.GROUP_JUMP: {
          sb.append(' ');
          int dest = offset + instr.operand(0);
          String destHex = Integer.toHexString(dest);
          for (int i = destHex.length(); i < digits; i++) sb.append('0');
          sb.append(destHex);
          break;
        }
        default: {
          switch (instr.opcode) {
            case CodeConstants.opc_new:
            case CodeConstants.opc_checkcast:
            case CodeConstants.opc_instanceof:
            case CodeConstants.opc_ldc:
            case CodeConstants.opc_ldc_w:
            case CodeConstants.opc_ldc2_w: {
              sb.append(' ');
              PooledConstant constant = pool.getConstant(instr.operand(0));
              if (constant.type == CodeConstants.CONSTANT_Dynamic && bootstrap != null) {
                appendBootstrapCall(sb, (LinkConstant) constant, bootstrap);
              } else {
                appendConstant(sb, constant);
              }
              break;
            }
            default: {
              for (int i = 0; i < instr.operandsCount(); i++) {
                sb.append(' ').append(instr.operand(i));
              }
            }
          }
        }
      }
      lines.add(sb.toString());
    }
  }

  private static void appendBootstrapCall(StringBuilder sb, LinkConstant target, StructBootstrapMethodsAttribute bootstrap) {
    sb.append(target.elementname).append(' ').append(target.descriptor);

    LinkConstant bsm = bootstrap.getMethodReference(target.index1);
    List<PooledConstant> bsmArgs = bootstrap.getMethodArguments(target.index1);

    sb.append(" bsm=");
    appendConstant(sb, bsm);
    sb.append(" args=[ ");
    boolean first = true;
    for (PooledConstant arg : bsmArgs) {
      if (!first) sb.append(", ");
      first = false;
      appendConstant(sb, arg);
    }
    sb.append(" ]");
  }

  private static void appendConstant(StringBuilder sb, PooledConstant constant) {
    if (constant == null) {
      sb.append("<null constant>");
      return;
    }
    if (constant instanceof PrimitiveConstant) {
      PrimitiveConstant prim = ((PrimitiveConstant) constant);
      Object value = prim.value;
      String stringValue = String.valueOf(value);
      if (prim.type == CodeConstants.CONSTANT_Class) {
        sb.append(stringValue);
      } else if (prim.type == CodeConstants.CONSTANT_String) {
        sb.append('"').append(ConstExprent.convertStringToJava(stringValue, false)).append('"');
      } else {
        sb.append(stringValue);
      }
    } else if (constant instanceof LinkConstant) {
      LinkConstant linkConstant = (LinkConstant) constant;
      sb.append(linkConstant.classname).append('.').append(linkConstant.elementname).append(' ').append(linkConstant.descriptor);
    }
  }

  public static boolean hideConstructor(ClassNode node, boolean init, boolean throwsExceptions, int paramCount, int methodAccessFlags) {
    if (!init || throwsExceptions || paramCount > 0 || !DecompilerContext.getOption(IFernflowerPreferences.HIDE_DEFAULT_CONSTRUCTOR)) {
      return false;
    }

    ClassWrapper wrapper = node.getWrapper();
    StructClass cl = wrapper.getClassStruct();

    int classAccessFlags = node.type == ClassNode.Type.ROOT ? cl.getAccessFlags() : node.access;
    boolean isEnum = cl.hasModifier(CodeConstants.ACC_ENUM) && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM);

    // default constructor requires same accessibility flags. Exception: enum constructor which is always private
    if (!isEnum && ((classAccessFlags & ACCESSIBILITY_FLAGS) != (methodAccessFlags & ACCESSIBILITY_FLAGS))) {
      return false;
    }

    int count = 0;
    for (StructMethod mt : cl.getMethods()) {
      if (CodeConstants.INIT_NAME.equals(mt.getName())) {
        if (++count > 1) {
          return false;
        }
      }
    }

    return true;
  }

  private static Map.Entry<VarType, GenericFieldDescriptor> getFieldTypeData(StructField fd) {
    VarType fieldType = new VarType(fd.getDescriptor(), false);

    GenericFieldDescriptor descriptor = fd.getSignature();
    return new AbstractMap.SimpleImmutableEntry<>(fieldType, descriptor);
  }

  private static boolean containsDeprecatedAnnotation(StructMember mb) {
    for (Key<?> key : ANNOTATION_ATTRIBUTES) {
      StructAnnotationAttribute attribute = (StructAnnotationAttribute) mb.getAttribute((Key<StructAnnotationAttribute>) key);
      if (attribute != null) {
        for (AnnotationExprent annotation : attribute.getAnnotations()) {
          if (annotation.getClassName().equals("java/lang/Deprecated")) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private static void appendDeprecation(TextBuffer buffer, int indent) {
    buffer.appendIndent(indent).append("/** @deprecated */").appendLineSeparator();
  }

  private enum MType {CLASS, FIELD, METHOD}

  private static void appendRenameComment(TextBuffer buffer, String oldName, MType type, int indent) {
    if (oldName == null) return;

    buffer.appendIndent(indent);
    buffer.append("// $VF: renamed from: ");

    switch (type) {
      case CLASS:
        buffer.append(ExprProcessor.buildJavaClassName(oldName));
        break;

      case FIELD:
        String[] fParts = oldName.split(" ");
        FieldDescriptor fd = FieldDescriptor.parseDescriptor(fParts[2]);
        buffer.append(fParts[1]);
        buffer.append(' ');
        buffer.append(getTypePrintOut(fd.type));
        break;

      default:
        String[] mParts = oldName.split(" ");
        MethodDescriptor md = MethodDescriptor.parseDescriptor(mParts[2]);
        buffer.append(mParts[1]);
        buffer.append(" (");
        boolean first = true;
        for (VarType paramType : md.params) {
          if (!first) {
            buffer.append(", ");
          }
          first = false;
          buffer.append(getTypePrintOut(paramType));
        }
        buffer.append(") ");
        buffer.append(getTypePrintOut(md.ret));
    }

    buffer.appendLineSeparator();
  }

  private static String getTypePrintOut(VarType type) {
    String typeText = ExprProcessor.getCastTypeName(type, false);
    if (ExprProcessor.UNDEFINED_TYPE_STRING.equals(typeText) &&
      DecompilerContext.getOption(IFernflowerPreferences.UNDEFINED_PARAM_TYPE_OBJECT)) {
      typeText = ExprProcessor.getCastTypeName(VarType.VARTYPE_OBJECT, false);
    }
    return typeText;
  }

  public static List<String> getErrorComment() {
    return Arrays.stream(((String) DecompilerContext.getProperty(IFernflowerPreferences.ERROR_MESSAGE)).split("\n")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
  }

  private static void appendComment(TextBuffer buffer, String comment, int indent) {
    buffer.appendIndent(indent).append("// $VF: ").append(comment).appendLineSeparator();
  }

  private static void appendInlineComment(TextBuffer buffer, String comment) {
    buffer.append("/* $VF: ").append(comment).append(" */");
  }

  private static void appendJavadoc(TextBuffer buffer, String javaDoc, int indent) {
    if (javaDoc == null) return;
    buffer.appendIndent(indent).append("/**").appendLineSeparator();
    for (String s : javaDoc.split("\n")) {
      buffer.appendIndent(indent).append(" * ").append(s).appendLineSeparator();
    }
    buffer.appendIndent(indent).append(" */").appendLineSeparator();
  }

  static final Key<?>[] ANNOTATION_ATTRIBUTES = {
    StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS, StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS};
  static final Key<?>[] PARAMETER_ANNOTATION_ATTRIBUTES = {
    StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS, StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS};
  static final Key<?>[] TYPE_ANNOTATION_ATTRIBUTES = {
    StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_TYPE_ANNOTATIONS, StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS};

  public static void appendAnnotations(TextBuffer buffer, int indent, StructMember mb, int targetType) {
    Set<String> filter = new HashSet<>();

    for (Key<?> key : ANNOTATION_ATTRIBUTES) {
      StructAnnotationAttribute attribute = mb.getAttribute((Key<StructAnnotationAttribute>) key);
      if (attribute != null) {
        for (AnnotationExprent annotation : attribute.getAnnotations()) {
          if (annotation.getClassName().equals("kotlin/Metadata")
            || annotation.getClassName().equals(NOT_NULL_ANN_NAME)
            || annotation.getClassName().equals(NULLABLE_ANN_NAME)
            || annotation.getClassName().equals("kotlin/jvm/JvmStatic")) {
            continue;
          }

          KAnnotationExprent kAnnotation = new KAnnotationExprent(annotation);

          String text = kAnnotation.toJava(indent).convertToStringAndAllowDataDiscard();
          filter.add(text);
          buffer.append(text);
          if (indent < 0) {
            buffer.append(' ');
          } else {
            buffer.appendLineSeparator();
          }
        }
      }
    }

    appendTypeAnnotations(buffer, indent, mb, targetType, -1, filter);
  }

  public static void appendJvmAnnotations(TextBuffer buffer, int indent, StructMember mb, boolean isInterface, ConstantPool pool, int targetType) {
    switch (targetType) {
      case TypeAnnotation.METHOD_RETURN_TYPE:
        if (isInterface && !mb.hasModifier(CodeConstants.ACC_ABSTRACT)) {
          buffer.appendIndent(indent).append("@JvmDefault").appendLineSeparator();
        }
        if (mb.hasModifier(CodeConstants.ACC_SYNCHRONIZED)) {
          buffer.appendIndent(indent).append("@Synchronized").appendLineSeparator();
        }
        if (mb.hasAttribute(StructGeneralAttribute.ATTRIBUTE_EXCEPTIONS)) {
          StructExceptionsAttribute attrib = mb.getAttribute(StructGeneralAttribute.ATTRIBUTE_EXCEPTIONS);
          buffer.appendIndent(indent).append("@Throws(");
          buffer.pushNewlineGroup(indent, 1);
          boolean first = true;
          for (int i : attrib.getThrowsExceptions()) {
            if (!first) {
              buffer.append(",").appendPossibleNewline(" ");
            }
            first = false;
            String name = pool.getPrimitiveConstant(i).getString();
            buffer.append(name).append("::class");
          }
          buffer.popNewlineGroup();
          buffer.append(")").appendLineSeparator();
        }
        break;
      case TypeAnnotation.FIELD:
        // TODO: use site targets
        if (mb.hasModifier(CodeConstants.ACC_TRANSIENT)) {
          buffer.appendIndent(indent).append("@Transient").appendLineSeparator();
        }
        if (mb.hasModifier(CodeConstants.ACC_VOLATILE)) {
          buffer.appendIndent(indent).append("@Volatile").appendLineSeparator();
        }
        break;
      case TypeAnnotation.CLASS_TYPE_PARAMETER:
        if (mb.hasAttribute(StructRecordAttribute.ATTRIBUTE_RECORD)) {
          buffer.appendIndent(indent).append("@JvmRecord").appendLineSeparator();
        }
    }

    if (mb.hasModifier(CodeConstants.ACC_STATIC) && targetType != TypeAnnotation.CLASS_TYPE_PARAMETER && KotlinDecompilationContext.getCurrentType() != KotlinDecompilationContext.KotlinType.FILE) {
      buffer.appendIndent(indent).append("@JvmStatic").appendLineSeparator();
    }
    if (mb.hasModifier(CodeConstants.ACC_STRICT)) {
      buffer.appendIndent(indent).append("@Strictfp").appendLineSeparator();
    }
    if (mb.hasModifier(CodeConstants.ACC_SYNTHETIC)) {
      buffer.appendIndent(indent).append("@JvmSynthetic").appendLineSeparator();
    }
  }

  static boolean isNullable(StructMember mb) {
    for (Key<?> key : ANNOTATION_ATTRIBUTES) {
      StructAnnotationAttribute attribute = (StructAnnotationAttribute) mb.getAttribute((Key<StructAnnotationAttribute>) key);
      if (attribute != null) {
        return attribute.getAnnotations().stream().anyMatch(annotation -> annotation.getClassName().equals(NULLABLE_ANN_NAME));
      }
    }
    return false;
  }

  // Returns true if a method with the given name and descriptor matches in the inheritance tree of the superclass.
  public static boolean searchForMethod(StructClass cl, String name, MethodDescriptor md, boolean search) {
    // Didn't find the class or the library containing the class wasn't loaded, can't search
    if (cl == null) {
      return false;
    }

    VBStyleCollection<StructMethod, String> methods = cl.getMethods();

    if (search) {
      // If we're allowed to search, iterate through the methods and try to find matches
      for (StructMethod method : methods) {
        // Match against name, descriptor, and whether or not the found method is static.
        // TODO: We are not handling generics or superclass parameters and return types
        if (md.equals(MethodDescriptor.parseDescriptor(method.getDescriptor())) && name.equals(method.getName()) && !method.hasModifier(CodeConstants.ACC_STATIC)) {
          return true;
        }
      }
    }

    // If we have a superclass that's not Object, search that as well
    if (cl.superClass != null) {
      StructClass superClass = DecompilerContext.getStructContext().getClass((String) cl.superClass.value);

      boolean foundInSuperClass = searchForMethod(superClass, name, md, true);

      if (foundInSuperClass) {
        return true;
      }
    }

    // Search all of the interfaces implemented by this class for the method
    for (String ifaceName : cl.getInterfaceNames()) {
      StructClass iface = DecompilerContext.getStructContext().getClass(ifaceName);

      boolean foundInIface = searchForMethod(iface, name, md, true);

      if (foundInIface) {
        return true;
      }
    }

    // We didn't manage to find anything, return
    return false;
  }

  public static boolean processParameterAnnotations(TextBuffer buffer, StructMethod mt, int param) {
    Set<String> filter = new HashSet<>();
    boolean ret = false;

    for (Key<?> key : PARAMETER_ANNOTATION_ATTRIBUTES) {
      StructAnnotationParameterAttribute attribute = (StructAnnotationParameterAttribute) mt.getAttribute((Key<StructAnnotationParameterAttribute>) key);
      if (attribute != null) {
        List<List<AnnotationExprent>> annotations = attribute.getParamAnnotations();
        if (param < annotations.size()) {
          for (AnnotationExprent annotation : annotations.get(param)) {
            if (annotation.getClassName().equals(NOT_NULL_ANN_NAME)) {
              continue;
            } else if (annotation.getClassName().equals(NULLABLE_ANN_NAME)) {
              ret = true;
              continue;
            }
            String text = annotation.toJava(-1).convertToStringAndAllowDataDiscard();
            filter.add(text);
            buffer.append(text).append(' ');
          }
        }
      }
    }

    appendTypeAnnotations(buffer, -1, mt, TypeAnnotation.METHOD_PARAMETER, param, filter);
    return ret;
  }

  private static void appendTypeAnnotations(TextBuffer buffer, int indent, StructMember mb, int targetType, int index, Set<String> filter) {
    for (Key<?> key : TYPE_ANNOTATION_ATTRIBUTES) {
      StructTypeAnnotationAttribute attribute = (StructTypeAnnotationAttribute) mb.getAttribute((Key<StructTypeAnnotationAttribute>) key);
      if (attribute != null) {
        for (TypeAnnotation annotation : attribute.getAnnotations()) {
          if (annotation.isTopLevel() && annotation.getTargetType() == targetType && (index < 0 || annotation.getIndex() == index)) {
            String text = annotation.getAnnotation().toJava(indent).convertToStringAndAllowDataDiscard();
            if (!filter.contains(text)) {
              buffer.append(text);
              if (indent < 0) {
                buffer.append(' ');
              } else {
                buffer.appendLineSeparator();
              }
            }
          }
        }
      }
    }
  }

  private static final Map<Integer, String> MODIFIERS;

  static {
    MODIFIERS = new LinkedHashMap<>();
    MODIFIERS.put(CodeConstants.ACC_PUBLIC, "public");
    MODIFIERS.put(CodeConstants.ACC_PROTECTED, "protected");
    MODIFIERS.put(CodeConstants.ACC_PRIVATE, "private");
    MODIFIERS.put(CodeConstants.ACC_ABSTRACT, "abstract");
//    MODIFIERS.put(CodeConstants.ACC_STATIC, "static");
//    MODIFIERS.put(CodeConstants.ACC_FINAL, "final");
//    MODIFIERS.put(CodeConstants.ACC_STRICT, "strictfp");
//    MODIFIERS.put(CodeConstants.ACC_TRANSIENT, "transient");
//    MODIFIERS.put(CodeConstants.ACC_VOLATILE, "volatile");
//    MODIFIERS.put(CodeConstants.ACC_SYNCHRONIZED, "synchronized");
    MODIFIERS.put(CodeConstants.ACC_NATIVE, "native");
  }

  private static final int CLASS_ALLOWED =
    CodeConstants.ACC_PROTECTED | CodeConstants.ACC_PRIVATE | CodeConstants.ACC_ABSTRACT |
      CodeConstants.ACC_STATIC | CodeConstants.ACC_STRICT;
  private static final int FIELD_ALLOWED =
    CodeConstants.ACC_PUBLIC | CodeConstants.ACC_PROTECTED | CodeConstants.ACC_PRIVATE | CodeConstants.ACC_STATIC |
      CodeConstants.ACC_FINAL | CodeConstants.ACC_TRANSIENT | CodeConstants.ACC_VOLATILE;
  private static final int METHOD_ALLOWED =
    CodeConstants.ACC_PUBLIC | CodeConstants.ACC_PROTECTED | CodeConstants.ACC_PRIVATE | CodeConstants.ACC_ABSTRACT |
      CodeConstants.ACC_STATIC | CodeConstants.ACC_FINAL | CodeConstants.ACC_SYNCHRONIZED | CodeConstants.ACC_NATIVE |
      CodeConstants.ACC_STRICT;

  private static final int CLASS_EXCLUDED = CodeConstants.ACC_ABSTRACT | CodeConstants.ACC_STATIC;
  private static final int FIELD_EXCLUDED = CodeConstants.ACC_PUBLIC | CodeConstants.ACC_STATIC | CodeConstants.ACC_FINAL;
  private static final int METHOD_EXCLUDED = CodeConstants.ACC_PUBLIC | CodeConstants.ACC_ABSTRACT;

  private static final int ACCESSIBILITY_FLAGS = CodeConstants.ACC_PUBLIC | CodeConstants.ACC_PROTECTED | CodeConstants.ACC_PRIVATE;

  private static void appendModifiers(TextBuffer buffer, int flags, int allowed, boolean isInterface, int excluded) {
    flags &= allowed;
    if (!isInterface) excluded = 0;
    for (int modifier : MODIFIERS.keySet()) {
      if ((flags & modifier) == modifier && (modifier & excluded) == 0) {
        buffer.append(MODIFIERS.get(modifier)).append(' ');
      }
    }
  }

  public static String getModifiers(int flags) {
    return MODIFIERS.entrySet().stream().filter(e -> (e.getKey() & flags) != 0).map(Map.Entry::getValue).collect(Collectors.joining(" "));
  }

  public static void appendTypeParameters(TextBuffer buffer, List<String> parameters, List<List<VarType>> bounds) {
    buffer.append('<');

    for (int i = 0; i < parameters.size(); i++) {
      if (i > 0) {
        buffer.append(", ");
      }

      buffer.append(parameters.get(i));

      List<VarType> parameterBounds = bounds.get(i);
      if (parameterBounds.size() > 1 || !"java/lang/Object".equals(parameterBounds.get(0).value)) {
        buffer.append(" extends ");
        buffer.append(ExprProcessor.getCastTypeName(parameterBounds.get(0)));
        for (int j = 1; j < parameterBounds.size(); j++) {
          buffer.append(" & ");
          buffer.append(ExprProcessor.getCastTypeName(parameterBounds.get(j)));
        }
      }
    }

    buffer.append('>');
  }

  private static void appendFQClassNames(TextBuffer buffer, List<String> names) {
    for (int i = 0; i < names.size(); i++) {
      String name = names.get(i);
      buffer.appendIndent(2).append(name);
      if (i < names.size() - 1) {
        buffer.append(',').appendLineSeparator();
      }
    }
  }
}
