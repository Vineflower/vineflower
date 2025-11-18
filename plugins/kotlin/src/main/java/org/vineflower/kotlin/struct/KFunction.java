package org.vineflower.kotlin.struct;

import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kt.metadata.deserialization.Flags;
import org.vineflower.kt.metadata.jvm.JvmProtoBuf;
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
import org.vineflower.kotlin.metadata.StructKotlinMetadataAttribute;
import org.vineflower.kotlin.util.KUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
) implements Flags {
  public static @NotNull Map<StructMethod, KFunction> parse(StructClass classStruct, StructKotlinMetadataAttribute ktData, @NotNull MetadataNameResolver resolver, List<ProtoBuf.Function> protoFunctions) {
    Map<StructMethod, KFunction> functions = new HashMap<>(protoFunctions.size(), 1f);

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
          throw new IllegalStateException("Couldn't find methodSupplier " + name + " " + desc + " in class " + classStruct.qualifiedName);
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

      Function<ClassWrapper, DefaultArgsMap> defaultArgs = wrapper -> DefaultArgsMap.from(wrapper.getMethodWrapper(defaultArgsName, defaultArgsDesc.toString()), wrapper.getMethodWrapper(method.getName(), method.getDescriptor()), parameters);

      ProtoBuf.Visibility visibility = VISIBILITY.get(flags);

      boolean knownOverride = visibility != ProtoBuf.Visibility.PRIVATE
        && visibility != ProtoBuf.Visibility.PRIVATE_TO_THIS
        && visibility != ProtoBuf.Visibility.LOCAL
        && KotlinWriter.searchForMethod(classStruct, method.getName(), method.methodDescriptor(), false);

      KContract contract = function.hasContract() ? KContract.from(function.getContract(), List.of(parameters), ktData) : null;

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

      functions.put(method, kFunction);
    }

    return functions;
  }

  public TextBuffer stringify(ClassWrapper wrapper, int indent) {
    TextBuffer buf = new TextBuffer();
    KotlinWriter.appendAnnotations(buf, indent, methodStruct, TypeAnnotation.METHOD_RETURN_TYPE);
    StructKotlinMetadataAttribute classData = classStruct.getAttribute(StructKotlinMetadataAttribute.KEY);
    boolean isInFile = classData != null && classData.metadata instanceof StructKotlinMetadataAttribute.File;
    KotlinWriter.appendJvmAnnotations(buf, indent, methodStruct, false, isInFile, classStruct.getPool(), TypeAnnotation.METHOD_RETURN_TYPE);

    String methodKey = InterpreterUtil.makeUniqueKey(methodStruct.getName(), methodStruct.getDescriptor());

    buf.appendIndent(indent);

    if (!contextReceiverTypes.isEmpty()) {
      buf.append("context(");
      boolean first = true;
      for (KType contextReceiverType : contextReceiverTypes) {
        if (!first) {
          buf.append(", ");
        }

        buf.append(contextReceiverType.stringify(indent + 1));
        first = false;
      }
      buf.append(")").appendLineSeparator().appendIndent(indent);
    }

    if (VISIBILITY.get(flags) != ProtoBuf.Visibility.PUBLIC || DecompilerContext.getOption(KotlinOptions.SHOW_PUBLIC_VISIBILITY)) {
      KUtils.appendVisibility(buf, VISIBILITY.get(flags));
    }

    if (IS_EXPECT_FUNCTION.get(flags)) {
      buf.append("expect ");
    }

    if (MODALITY.get(flags) != ProtoBuf.Modality.FINAL) {
      if (!knownOverride || MODALITY.get(flags) != ProtoBuf.Modality.OPEN) {
        buf.append(MODALITY.get(flags).name().toLowerCase())
          .append(' ');
      }
    }

    if (IS_EXTERNAL_FUNCTION.get(flags)) {
      buf.append("external ");
    }

    if (knownOverride) {
      buf.append("override ");
    }

    if (IS_TAILREC.get(flags)) {
      buf.append("tailrec ");
    }

    if (IS_SUSPEND.get(flags)) {
      buf.append("suspend ");
    }

    if (IS_INLINE.get(flags)) {
      buf.append("inline ");
    }

    if (IS_INFIX.get(flags)) {
      buf.append("infix ");
    }

    if (IS_OPERATOR.get(flags)) {
      buf.append("operator ");
    }

    buf.append("fun ");

    List<KTypeParameter> complexTypeParams = typeParameters.stream()
      .filter(typeParameter -> typeParameter.upperBounds().size() > 1)
      .toList();

    if (!typeParameters.isEmpty()) {
      buf.append('<');

      for (int i = 0; i < typeParameters.size(); i++) {
        KTypeParameter typeParameter = typeParameters.get(i);

        if (typeParameter.reified()) {
          buf.append("reified ");
        }

        buf.append(KotlinWriter.toValidKotlinIdentifier(typeParameter.name()));

        if (typeParameter.upperBounds().size() == 1) {
          buf.append(" : ").append(typeParameter.upperBounds().get(0).stringify(indent + 1));
        }

        if (i < typeParameters.size() - 1) {
          buf.append(", ");
        }
      }

      buf.append("> ");
    }

    if (receiverType != null) {
      // Function types need parentheses around the receiver type, but that happens in KType.stringify only if it's nullable
      // so we need to wrap in the case of non-nullable function types
      if (!receiverType.isNullable && receiverType.kotlinType.startsWith("kotlin/Function")) {
        buf.append("(");
      }
      buf.append(receiverType.stringify(indent + 1));
      if (!receiverType.isNullable && receiverType.kotlinType.startsWith("kotlin/Function")) {
        buf.append(")");
      }

      buf.append(".");
    }

    buf.append(KotlinWriter.toValidKotlinIdentifier(name))
      .append('(')
      .pushNewlineGroup(indent, 1)
      .appendPossibleNewline("");

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

    buf.appendPossibleNewline("", true)
      .popNewlineGroup()
      .append(')');

    if (returnType != null && returnType.type != VarType.VARTYPE_VOID.type) {
      buf.append(": ")
        .append(returnType.stringify(indent + 1));
    }

    if (complexTypeParams.isEmpty()) {
      buf.append(' ');
    } else {
      buf.pushNewlineGroup(indent, 1)
        .appendPossibleNewline(" ")
        .append("where ");

      first = true;
      for (KTypeParameter typeParameter : complexTypeParams) {
        for (KType upperBound : typeParameter.upperBounds()) {
          if (!first) {
            buf.appendPossibleNewline(",").appendPossibleNewline(" ");
          }

          buf.append(KotlinWriter.toValidKotlinIdentifier(typeParameter.name()))
            .append(" : ")
            .append(upperBound.stringify(indent + 1));

          first = false;
        }
      }

      buf.appendPossibleNewline(" ", true)
        .popNewlineGroup();
    }

    buf.append('{').appendLineSeparator();

    if (contract != null) {
      buf.append(contract.stringify(indent + 1));
    }

    MethodWrapper methodWrapper = methodSupplier.apply(wrapper);
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

    buf.appendIndent(indent).append('}').appendLineSeparator();

    return buf;
  }
}
