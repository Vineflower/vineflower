package org.vineflower.kotlin.struct;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
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
import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kt.metadata.deserialization.Flags;
import org.vineflower.kt.metadata.jvm.JvmProtoBuf;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record KFunction(
  String name,
  KParameter[] parameters,
  List<KTypeParameter> typeParameters,
  KType returnType,
  int flags,
  List<KType> contextReceiverTypes,
  Function<ClassWrapper, MethodWrapper> methodSupplier,
  @Nullable KType receiverType,
  @Nullable KContract contract,
  boolean knownOverride,
  @NotNull Function<ClassWrapper, @NotNull DefaultArgsMap> defaultArgsSupplier,
  StructClass classStruct,
  StructMethod methodStruct
) implements KElement, Flags {
  public static final KFunction FAILED_LAMBDA = new KFunction(
    "failed-lambda",
    new KParameter[0],
    List.of(),
    KType.NOTHING,
    0,
    List.of(),
    wrapper -> null,
    null,
    null,
    false,
    wrapper -> {
      throw new IllegalStateException("Attempted to read default arguments for failed lambda");
    },
    null,
    null
  );

  public static void parse(StructClass classStruct, @Nullable MetadataNameResolver resolver, ProtoBuf.TypeTable typeTable, List<ProtoBuf.Function> protoFunctions, boolean isSyntheticFunction, StructClass companionParent) {
    if (resolver == null) {
      return;
    }

    for (ProtoBuf.Function function : protoFunctions) {
      JvmProtoBuf.JvmMethodSignature jvmData = function.getExtension(JvmProtoBuf.methodSignature);

      int flags = function.getFlags();

      String name = resolver.resolve(function.getName());

      KParameter[] parameters = new KParameter[function.getValueParameterCount()];
      for (int i = 0; i < parameters.length; i++) {
        ProtoBuf.ValueParameter parameter = function.getValueParameter(i);
        int paramFlags = parameter.getFlags();
        String paramName = resolver.resolve(parameter.getName());
        KType paramType = KType.from(parameter.getType(), resolver);
        KType varargType = parameter.hasVarargElementType() ? KType.from(parameter.getVarargElementType(), resolver) : null;
        int typeId = parameter.getTypeId();
        parameters[i] = new KParameter(paramFlags, paramName, paramType, varargType, typeId);
      }

      KType receiverType = null;
      if (function.hasReceiverType()) {
        receiverType = KType.from(function.getReceiverType(), resolver);
      }

      KType returnType = KType.from(function.getReturnType(), resolver);

      StructMethod methodLookup = null;
      String lookupName = jvmData.hasName() ? resolver.resolve(jvmData.getName()) : name;
      if (jvmData.hasDesc()) {
        methodLookup = classStruct.getMethod(lookupName, resolver.resolve(jvmData.getDesc()));
      }

      if (methodLookup == null) {
        StringBuilder desc = new StringBuilder("(");
        if (receiverType != null) {
          desc.append(receiverType);
        }

        for (KParameter parameter : parameters) {
          desc.append(parameter.type());
        }

        desc.append(")").append(returnType);

        methodLookup = classStruct.getMethod(lookupName, desc.toString());

        if (methodLookup == null) {
          if (IS_SUSPEND.get(flags) && "<anonymous>".equals(name)) {
            //TODO suspend function support at large
            continue;
          }
          DecompilerContext.getLogger().writeMessage("Couldn't find method " + name + " " + desc + " in class " + classStruct.qualifiedName, IFernflowerLogger.Severity.ERROR);
          continue;
        }

        if (companionParent != null) {
          StructMethod inParent = companionParent.getMethod(lookupName, desc.toString());
          if (inParent != null) {
            inParent.getAttributes().put(KElement.KEY, KHiddenElement.COMPANION_ITEM);
          }
        }
      }
      @NotNull StructMethod method = methodLookup;

      List<KTypeParameter> typeParameters = function.getTypeParameterList().stream()
        .map(typeParameter -> KTypeParameter.from(typeParameter, resolver))
        .collect(Collectors.toList());

      List<KType> contextReceiverTypes = function.getContextReceiverTypeList().stream()
        .map(ctxType -> KType.from(ctxType, resolver))
        .collect(Collectors.toList());

      boolean isStatic = method.hasModifier(CodeConstants.ACC_STATIC);
      String defaultArgsName = name + "$default";
      StringBuilder defaultArgsDesc = new StringBuilder("(");
      if (!isStatic) {
        defaultArgsDesc.append("L").append(classStruct.qualifiedName).append(";");
      }
      if (receiverType != null) {
        defaultArgsDesc.append(receiverType);
      }
      for (KParameter parameter : parameters) {
        if (parameter.type().typeParameterName != null) {
          typeParameters.stream()
            .filter(typeParameter -> typeParameter.name().equals(parameter.type().typeParameterName))
            .findAny()
            .map(KTypeParameter::upperBounds)
            .filter(bounds -> bounds.size() == 1)
            .map(bounds -> bounds.get(0))
            .ifPresentOrElse(defaultArgsDesc::append, () -> defaultArgsDesc.append("Ljava/lang/Object;"));
        } else {
          defaultArgsDesc.append(parameter.type());
        }
      }

      defaultArgsDesc.append("I".repeat(parameters.length / 32 + 1));
      defaultArgsDesc.append("Ljava/lang/Object;)");
      defaultArgsDesc.append(returnType);

      StructMethod defaultMethod = classStruct.getMethod(defaultArgsName, defaultArgsDesc.toString());
      if (defaultMethod != null) {
        defaultMethod.getAttributes().put(KElement.KEY, KHiddenElement.DEFAULT_IMPL);
      }
      
      if (companionParent != null) {
        StructMethod inParent = companionParent.getMethod(defaultArgsName, defaultArgsDesc.toString());
        if (inParent != null) {
          inParent.getAttributes().put(KElement.KEY, KHiddenElement.DEFAULT_IMPL);
        }
      }

      Function<ClassWrapper, DefaultArgsMap> defaultArgs = wrapper -> DefaultArgsMap.from(wrapper.getMethodWrapper(defaultArgsName, defaultArgsDesc.toString()), wrapper.getMethodWrapper(method.getName(), method.getDescriptor()), parameters);

      ProtoBuf.Visibility visibility = VISIBILITY.get(flags);

      boolean knownOverride = visibility != ProtoBuf.Visibility.PRIVATE
        && visibility != ProtoBuf.Visibility.PRIVATE_TO_THIS
        && visibility != ProtoBuf.Visibility.LOCAL
        && KotlinWriter.searchForMethod(classStruct, method.getName(), method.methodDescriptor(), false);

      KContract contract = function.hasContract() ? KContract.from(function.getContract(), List.of(parameters), resolver, typeTable) : null;

      KFunction kFunction = new KFunction(
        name,
        parameters,
        typeParameters,
        returnType,
        flags,
        contextReceiverTypes,
        wrapper -> wrapper.getMethodWrapper(method.getName(), method.getDescriptor()),
        receiverType,
        contract,
        knownOverride,
        defaultArgs,
        classStruct,
        method
      );

      method.getAttributes().put(KElement.KEY, kFunction);

      if (isSyntheticFunction) {
        classStruct.getAttributes().put(KElement.KEY, kFunction);
      }
    }
  }

  public TextBuffer stringify(ClassWrapper wrapper, int indent) {
    TextBuffer buf = new TextBuffer();
    KotlinWriter.appendAnnotations(buf, indent, methodStruct, TypeAnnotation.METHOD_RETURN_TYPE);
    KElement classData = classStruct.getAttribute(KElement.KEY);
    boolean isInFile = classData instanceof KFile;
    KotlinWriter.appendJvmAnnotations(buf, indent, methodStruct, false, isInFile, classStruct.getPool(), TypeAnnotation.METHOD_RETURN_TYPE);

    String methodKey = InterpreterUtil.makeUniqueKey(methodStruct.getName(), methodStruct.getDescriptor());

    buf.appendIndent(indent);

    if (!contextReceiverTypes.isEmpty()) {
      buf.appendKeyword("context").appendPunctuation("(");
      boolean first = true;
      for (KType contextReceiverType : contextReceiverTypes) {
        if (!first) {
          buf.appendPunctuation(",").appendWhitespace(" ");
        }

        buf.append(contextReceiverType.stringify(indent + 1));
        first = false;
      }
      buf.appendPunctuation(")").appendLineSeparator().appendIndent(indent);
    }

    if (VISIBILITY.get(flags) != ProtoBuf.Visibility.PUBLIC || DecompilerContext.getOption(KotlinOptions.SHOW_PUBLIC_VISIBILITY)) {
      KUtils.appendVisibility(buf, VISIBILITY.get(flags));
    }

    if (IS_EXPECT_FUNCTION.get(flags)) {
      buf.appendKeyword("expect").appendWhitespace(" ");
    }

    if (MODALITY.get(flags) != ProtoBuf.Modality.FINAL) {
      if (!knownOverride || MODALITY.get(flags) != ProtoBuf.Modality.OPEN) {
        buf.appendKeyword(MODALITY.get(flags).name().toLowerCase())
          .appendWhitespace(" ");
      }
    }

    if (IS_EXTERNAL_FUNCTION.get(flags)) {
      buf.appendKeyword("external").appendWhitespace(" ");
    }

    if (knownOverride) {
      buf.appendKeyword("override").appendWhitespace(" ");
    }

    if (IS_TAILREC.get(flags)) {
      buf.appendKeyword("tailrec").appendWhitespace(" ");
    }

    if (IS_SUSPEND.get(flags)) {
      buf.appendKeyword("suspend").appendWhitespace(" ");
    }

    if (IS_INLINE.get(flags)) {
      buf.appendKeyword("inline").appendWhitespace(" ");
    }

    if (IS_INFIX.get(flags)) {
      buf.appendKeyword("infix").appendWhitespace(" ");
    }

    if (IS_OPERATOR.get(flags)) {
      buf.appendKeyword("operator").appendWhitespace(" ");
    }

    buf.appendKeyword("fun").appendWhitespace(" ");

    List<KTypeParameter> complexTypeParams = typeParameters.stream()
      .filter(typeParameter -> typeParameter.upperBounds().size() > 1)
      .toList();

    if (!typeParameters.isEmpty()) {
      buf.appendPunctuation('<');

      for (int i = 0; i < typeParameters.size(); i++) {
        KTypeParameter typeParameter = typeParameters.get(i);

        if (typeParameter.reified()) {
          buf.appendKeyword("reified").appendWhitespace(" ");
        }

        buf.appendGeneric(KotlinWriter.toValidKotlinIdentifier(typeParameter.name()), false, classStruct.qualifiedName, name, methodStruct.getDescriptor());

        if (typeParameter.upperBounds().size() == 1) {
          buf.appendWhitespace(" ").appendPunctuation(":").appendWhitespace(" ").append(typeParameter.upperBounds().get(0).stringify(indent + 1));
        }

        if (i < typeParameters.size() - 1) {
          buf.appendPunctuation(",").appendWhitespace(" ");
        }
      }

      buf.appendPunctuation(">").appendWhitespace(" ");
    }

    if (receiverType != null) {
      // Function types need parentheses around the receiver type, but that happens in KType.stringify only if it's nullable
      // so we need to wrap in the case of non-nullable function types
      if (!receiverType.isNullable && receiverType.kotlinType.startsWith("kotlin/Function")) {
        buf.appendPunctuation("(");
      }
      buf.append(receiverType.stringify(indent + 1));
      if (!receiverType.isNullable && receiverType.kotlinType.startsWith("kotlin/Function")) {
        buf.appendPunctuation(")");
      }

      buf.appendPunctuation(".");
    }

    buf.appendMethod(KotlinWriter.toValidKotlinIdentifier(name), true, classStruct.qualifiedName, methodStruct.getName(), methodStruct.getDescriptor())
      .appendPunctuation('(')
      .pushNewlineGroup(indent, 1)
      .appendPossibleNewline("");

    boolean first = true;
    for (KParameter parameter : parameters) {
      if (!first) {
        buf.appendPunctuation(",").appendPossibleNewline(" ");
      }

      first = false;

      parameter.stringify(indent + 1, buf);
      if (DECLARES_DEFAULT_VALUE.get(parameter.flags())) {
        buf.append(defaultArgsSupplier.apply(wrapper).toJava(parameter, indent + 1), classStruct.qualifiedName, methodKey);
      }
    }

    buf.appendPossibleNewline("", true)
      .popNewlineGroup()
      .appendPunctuation(')');

    if (returnType != null && returnType.type != VarType.VARTYPE_VOID.type) {
      buf.appendPunctuation(":").appendWhitespace(" ")
        .append(returnType.stringify(indent + 1));
    }

    if (complexTypeParams.isEmpty()) {
      buf.appendWhitespace(" ");
    } else {
      buf.pushNewlineGroup(indent, 1)
        .appendPossibleNewline(" ")
        .appendKeyword("where").appendWhitespace(" ");

      first = true;
      for (KTypeParameter typeParameter : complexTypeParams) {
        for (KType upperBound : typeParameter.upperBounds()) {
          if (!first) {
            buf.appendPossibleNewline(",").appendPossibleNewline(" ");
          }

          buf.appendGeneric(KotlinWriter.toValidKotlinIdentifier(typeParameter.name()), false, classStruct.qualifiedName, name, methodStruct().getDescriptor())
            .appendWhitespace(" ").appendPunctuation(":").appendWhitespace(" ")
            .append(upperBound.stringify(indent + 1));

          first = false;
        }
      }

      buf.appendPossibleNewline(" ", true)
        .popNewlineGroup();
    }

    buf.appendPunctuation('{').appendLineSeparator();

    MethodWrapper methodWrapper = methodSupplier.apply(wrapper);
    MethodWrapper parent = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
    try {
      DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, methodWrapper);
      if (contract != null) {
        buf.append(contract.stringify(indent + 1));
      }

      RootStatement root = methodWrapper.root;
      if (root != null && methodWrapper.decompileError == null) {
        try {
          TextBuffer body = root.toJava(indent + 1);
          body.addBytecodeMapping(root.getDummyExit().bytecode);
          if (body.length() != 0 && contract != null) {
            buf.appendLineSeparator();
          }

          buf.append(body, classStruct.qualifiedName, methodKey);
        } catch (Throwable t) {
          String message = "Method " + methodStruct.getName() + " " + methodWrapper.desc() + " in class " + classStruct.qualifiedName + " couldn't be written.";
          DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN, t);
          methodWrapper.decompileError = t;
        }
      }

      if (methodWrapper.decompileError != null) {
        KotlinWriter.dumpError(buf, methodWrapper, indent + 1);
      }

      buf.appendIndent(indent).appendPunctuation('}').appendLineSeparator();
    } finally {
      DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, parent);
    }

    return buf;
  }
}
