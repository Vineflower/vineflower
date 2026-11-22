package org.vineflower.kotlin.struct;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.util.Key;
import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kt.metadata.deserialization.Flags;
import org.vineflower.kt.metadata.jvm.JvmProtoBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.TypeAnnotation;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinOptions;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.KUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record KConstructor(
  KParameter[] parameters,
  int flags,
  Function<ClassWrapper, MethodWrapper> methodSupplier,
  boolean isPrimary,
  Function<ClassWrapper, DefaultArgsMap> defaultArgsSupplier,
  StructClass classStruct,
  StructMethod methodStruct,
  int classFlags
) implements KElement, Flags {
  public static final Key<KConstructor> PRIMARY_CONSTRUCTOR = Key.of("primary-constructor");

  private static final VarType DEFAULT_CONSTRUCTOR_MARKER = new VarType("kotlin/jvm/internal/DefaultConstructorMarker", true);

  public static void parse(StructClass classStruct, ProtoBuf.Class protoClass, @Nullable MetadataNameResolver resolver) {
    if (resolver == null) {
      return;
    }

    int classFlags = protoClass.getFlags();
    if (MODALITY.get(classFlags) == ProtoBuf.Modality.ABSTRACT) return;

    List<ProtoBuf.Constructor> protoConstructors = protoClass.getConstructorList();
    if (protoConstructors.isEmpty()) return;

    Map<StructMethod, KConstructor> constructors = new HashMap<>();
    KConstructor primary = null;

    for (ProtoBuf.Constructor constructor : protoConstructors) {
      KParameter[] parameters = new KParameter[constructor.getValueParameterCount()];
      for (int i = 0; i < parameters.length; i++) {
        ProtoBuf.ValueParameter protoParameter = constructor.getValueParameter(i);
        parameters[i] = new KParameter(
          protoParameter.getFlags(),
          resolver.resolve(protoParameter.getName()),
          KType.from(protoParameter.getType(), resolver),
          KType.from(protoParameter.getVarargElementType(), resolver),
          protoParameter.getTypeId()
        );
      }

      int flags = constructor.getFlags();

      JvmProtoBuf.JvmMethodSignature signature = constructor.getExtension(JvmProtoBuf.constructorSignature);
      String desc = resolver.resolve(signature.getDesc());
      StructMethod method = classStruct.getMethod("<init>", desc);
      if (method == null) {
        if (CLASS_KIND.get(classFlags) == ProtoBuf.Class.Kind.ANNOTATION_CLASS) {
          // Annotation classes are very odd and don't actually have a constructor under the hood
          KConstructor kConstructor = new KConstructor(parameters, flags, null, true, ignored -> DefaultArgsMap.fromAnnotation(classStruct), classStruct, null, classFlags);
          classStruct.getAttributes().put(PRIMARY_CONSTRUCTOR, kConstructor);
          return;
        }

        DecompilerContext.getLogger().writeMessage("Method <init>" + desc + " not found in " + classStruct.qualifiedName, IFernflowerLogger.Severity.WARN);
        continue;
      }

      boolean isPrimary = !IS_SECONDARY.get(flags);

      StringBuilder defaultArgsDesc = new StringBuilder("(");
      if (CLASS_KIND.get(classFlags) == ProtoBuf.Class.Kind.ENUM_CLASS) {
        // Kotlin drops hidden name/ordinal parameters for enum allConstructors in its metadata
        defaultArgsDesc.append("Ljava/lang/String;").append("I");
      }

      for (KParameter parameter : parameters) {
        defaultArgsDesc.append(parameter.type());
      }

      defaultArgsDesc.append("I".repeat(parameters.length / 32 + 1));
      defaultArgsDesc.append("Lkotlin/jvm/internal/DefaultConstructorMarker;)V");

      StructMethod defaultMethod = classStruct.getMethod("<init>", defaultArgsDesc.toString());
      if (defaultMethod != null) {
        defaultMethod.getAttributes().put(KElement.KEY, KHiddenElement.DEFAULT_IMPL);
      }

      Function<ClassWrapper, DefaultArgsMap> defaultArgsSupplier = wrapper -> DefaultArgsMap.from(wrapper.getMethodWrapper("<init>", defaultArgsDesc.toString()), wrapper.getMethodWrapper(method.getName(), method.getDescriptor()), parameters);

      KConstructor kConstructor = new KConstructor(parameters, flags, wrapper -> wrapper.getMethodWrapper(method.getName(), method.getDescriptor()), isPrimary, defaultArgsSupplier, classStruct, method, classFlags);
      method.getAttributes().put(KElement.KEY, kConstructor);

      if (isPrimary) {
        classStruct.getAttributes().put(PRIMARY_CONSTRUCTOR, kConstructor);
      }
    }
  }

  private boolean shouldHideConstructor() {
    if (!isPrimary || parameters.length > 0 || !DecompilerContext.getOption(IFernflowerPreferences.HIDE_DEFAULT_CONSTRUCTOR)) {
      return false;
    }

    if (VISIBILITY.get(flags) != VISIBILITY.get(classFlags) && CLASS_KIND.get(classFlags) != ProtoBuf.Class.Kind.ENUM_CLASS) {
      return false;
    }

    return true;
  }

  public boolean stringify(ClassWrapper wrapper, TextBuffer buffer, int indent) {
    if (shouldHideConstructor()) {
      return false;
    }

    TextBuffer buf = new TextBuffer();
    MethodWrapper methodWrapper = methodSupplier.apply(wrapper);
    RootStatement root = methodWrapper.root;

    String methodKey = InterpreterUtil.makeUniqueKey(methodWrapper.methodStruct.getName(), methodWrapper.methodStruct.getDescriptor());

    if (!isPrimary) {
      if (HAS_ANNOTATIONS.get(flags)) {
        KotlinWriter.appendAnnotations(buf, indent, methodWrapper.methodStruct, TypeAnnotation.METHOD_RETURN_TYPE);
        KotlinWriter.appendJvmAnnotations(buf, indent, methodWrapper.methodStruct, false, false, methodWrapper.classStruct.getPool(), TypeAnnotation.METHOD_RETURN_TYPE);
      }

      buf.appendIndent(indent);

      if (VISIBILITY.get(flags) != ProtoBuf.Visibility.PUBLIC || DecompilerContext.getOption(KotlinOptions.SHOW_PUBLIC_VISIBILITY)) {
        KUtils.appendVisibility(buf, VISIBILITY.get(flags));
      }

      buf.append("constructor");

      buf.append("(").pushNewlineGroup(indent, 1);

      boolean first = true;
      for (KParameter parameter : parameters) {
        if (!first) {
          buf.append(",").appendPossibleNewline(" ");
        }

        first = false;

        parameter.stringify(indent + 1, buf);

        if (DECLARES_DEFAULT_VALUE.get(parameter.flags())) {
          buf.append(defaultArgsSupplier.apply(wrapper).toJava(parameter, indent + 1), classStruct.qualifiedName, methodKey);
        }
      }

      buf.appendPossibleNewline("", true).popNewlineGroup();

      String methodDescriptor = methodStruct.getName() + methodStruct.getDescriptor();
      String containingClass = classStruct.qualifiedName;

      List<Exprent> exprents = methodWrapper.getOrBuildGraph().first.exprents;
      if (exprents.isEmpty()) {
        DecompilerContext.getLogger().writeMessage("Unexpected empty constructor body in " + containingClass + " " + methodDescriptor, IFernflowerLogger.Severity.WARN);
        return true;
      }

      buf.append(") ");

      Exprent firstExpr = exprents.get(0);
      if (!(firstExpr instanceof InvocationExprent)) {
        // no detected super / this constructor call (something isn't right)
        DecompilerContext.getLogger().writeMessage("Unexpected missing super/this constructor call in " + containingClass + " " + methodDescriptor, IFernflowerLogger.Severity.WARN);
      } else {
        buf.append(": ");

        buf.append(firstExpr.toJava(indent + 1), classStruct.qualifiedName, methodKey);

        methodWrapper.getOrBuildGraph().first.exprents.remove(0);
      }
    }

    if (methodWrapper.getOrBuildGraph().first.exprents.isEmpty()) {
      // There is no extra body so all done!
      if (isPrimary) return false; // avoid extra empty line

      buffer.append(buf);
      return true;
    }

    if (isPrimary) {
      buf.appendIndent(indent).append("init");
    }

    buf.append(" {").appendLineSeparator();

    TextBuffer body = root.toJava(indent + 1);
    body.addBytecodeMapping(root.getDummyExit().bytecode);

    buf.append(body, classStruct.qualifiedName, methodKey);

    buf.appendIndent(indent).append("}").appendLineSeparator();

    buffer.append(buf);
    return true;
  }

  public boolean writePrimaryConstructor(ClassWrapper wrapper, TextBuffer buffer, int indent) {
    if (!isPrimary) return false;

    TextBuffer buf = new TextBuffer();
    boolean appended = false;

    String methodKey = InterpreterUtil.makeUniqueKey(methodStruct.getName(), methodStruct.getDescriptor());

    if (CLASS_KIND.get(classFlags) != ProtoBuf.Class.Kind.OBJECT && CLASS_KIND.get(classFlags) != ProtoBuf.Class.Kind.COMPANION_OBJECT) {
      if (HAS_ANNOTATIONS.get(flags)) {
        buf.append(" ");
        // -1 for indent indicates inline
        KotlinWriter.appendAnnotations(buf, -1, methodStruct, TypeAnnotation.METHOD_RETURN_TYPE);
        KotlinWriter.appendJvmAnnotations(buf, -1, methodStruct, false, false, classStruct.getPool(), TypeAnnotation.METHOD_RETURN_TYPE);
        appended = true;
      }

      // For cleanliness, public primary allConstructors are not forced public by the config option
      if ((VISIBILITY.get(flags) != ProtoBuf.Visibility.PUBLIC || (appended && DecompilerContext.getOption(KotlinOptions.SHOW_PUBLIC_VISIBILITY))) &&
        CLASS_KIND.get(classFlags) != ProtoBuf.Class.Kind.ENUM_CLASS // Enum allConstructors are always private implicitly
      ) {
        buf.append(" ");
        KUtils.appendVisibility(buf, VISIBILITY.get(flags));
        appended = true;
      }

      if (appended) {
        buf.append("constructor");
      }

      if (parameters.length > 0 || appended) {
        buf.append("(").pushNewlineGroup(indent, 1);

        boolean first = true;
        for (KParameter parameter : parameters) {
          if (!first) {
            buf.append(",").appendPossibleNewline(" ");
          }

          first = false;

          parameter.stringify(indent + 1, buf);

          if (DECLARES_DEFAULT_VALUE.get(parameter.flags())) {
            buf.append(defaultArgsSupplier.apply(wrapper).toJava(parameter, indent + 1), classStruct.qualifiedName, methodKey);
          }
        }

        buf.appendPossibleNewline("", true).popNewlineGroup().append(")");
      }
    }

    MethodWrapper methodWrapper = methodSupplier.apply(wrapper);

    if (methodWrapper.getOrBuildGraph().first.exprents.isEmpty()) {
      // No ability to declare super constructor call
      buffer.append(buf);
      return false;
    }

    Exprent firstExpr = methodWrapper.getOrBuildGraph().first.exprents.get(0);
    if (!(firstExpr instanceof InvocationExprent invocation) || !invocation.getName().equals("<init>")) {
      // no detected super constructor call
      buffer.append(buf);
      return false;
//      throw new IllegalStateException("First expression of constructor is not InvocationExprent");
    }

    if (invocation.getClassname().equals("java/lang/Object") || CLASS_KIND.get(classFlags) == ProtoBuf.Class.Kind.ENUM_CLASS) {
      // No need to declare super constructor call
      buffer.append(buf);
      return false;
    }

    ImportCollector imports = DecompilerContext.getImportCollector();
    String superClass = imports.getShortName(invocation.getClassname().replace('/', '.'));
    buf.append(" : ");

    // replace "super" with the actual class name
    buf.append(superClass).append('(');

    KUtils.removeArguments(invocation, DEFAULT_CONSTRUCTOR_MARKER);

    buf.append(invocation.appendParamList(indent + 1));
    buf.append(")");

    buf.addBytecodeMapping(invocation.bytecode);

    methodWrapper.getOrBuildGraph().first.exprents.remove(0);

    buffer.append(buf, classStruct.qualifiedName, methodKey);
    return true;
  }
}
