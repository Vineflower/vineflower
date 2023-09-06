package org.vineflower.kotlin.struct;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import kotlin.reflect.jvm.internal.impl.metadata.jvm.JvmProtoBuf;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.TypeAnnotation;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinDecompilationContext;
import org.vineflower.kotlin.KotlinPreferences;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.KUtils;
import org.vineflower.kotlin.util.ProtobufFlags;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KFunction {
  public final String name;
  public final ProtobufFlags.Function flags;
  public final KParameter[] parameters;
  public final List<KTypeParameter> typeParameters;
  public final KType returnType;

  public final MethodWrapper method;

  @Nullable
  public final KType receiverType;

  private final ClassesProcessor.ClassNode node;

  private KFunction(
    String name,
    KParameter[] parameters,
    List<KTypeParameter> typeParameters,
    KType returnType,
    ProtobufFlags.Function flags,
    MethodWrapper method,
    @Nullable KType receiverType,
    ClassesProcessor.ClassNode node) {
    this.name = name;
    this.parameters = parameters;
    this.typeParameters = typeParameters;
    this.returnType = returnType;
    this.flags = flags;
    this.method = method;
    this.receiverType = receiverType;
    this.node = node;
  }

  public static Map<StructMethod, KFunction> parse(ClassesProcessor.ClassNode node) {
    MetadataNameResolver resolver = KotlinDecompilationContext.getNameResolver();
    ClassWrapper wrapper = node.getWrapper();
    StructClass struct = wrapper.getClassStruct();

    List<ProtoBuf.Function> protoFunctions;

    KotlinDecompilationContext.KotlinType type = KotlinDecompilationContext.getCurrentType();
    if (type == null) return Map.of();

    switch (type) {
      case CLASS:
        protoFunctions = KotlinDecompilationContext.getCurrentClass().getFunctionList();
        break;
      case FILE:
        protoFunctions = KotlinDecompilationContext.getFilePackage().getFunctionList();
        break;
      case MULTIFILE_CLASS:
        protoFunctions = KotlinDecompilationContext.getMultifilePackage().getFunctionList();
        break;
      case SYNTHETIC_CLASS:
        // indicating lambdas and such
        protoFunctions = Collections.singletonList(KotlinDecompilationContext.getSyntheticClass());
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }

    Map<StructMethod, KFunction> functions = new HashMap<>(protoFunctions.size(), 1f);

    for (ProtoBuf.Function function : protoFunctions) {
      JvmProtoBuf.JvmMethodSignature jvmData = function.getExtension(JvmProtoBuf.methodSignature);

      ProtobufFlags.Function flags = new ProtobufFlags.Function(function.getFlags());

      String name = resolver.resolve(function.getName());

      KParameter[] parameters = new KParameter[function.getValueParameterCount()];
      for (int i = 0; i < parameters.length; i++) {
        ProtoBuf.ValueParameter parameter = function.getValueParameter(i);
        ProtobufFlags.ValueParameter paramFlags = new ProtobufFlags.ValueParameter(parameter.getFlags());
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

      StringBuilder desc = new StringBuilder("(");
      if (receiverType != null) {
        desc.append(receiverType);
      }

      for (KParameter parameter : parameters) {
        desc.append(parameter.type);
      }

      desc.append(")").append(returnType);

      MethodWrapper method = wrapper.getMethodWrapper(name, desc.toString());

      List<KTypeParameter> typeParameters = function.getTypeParameterList().stream()
        .map(typeParameter -> KTypeParameter.from(typeParameter, resolver))
        .collect(Collectors.toList());

      functions.put(method.methodStruct, new KFunction(name, parameters, typeParameters, returnType, flags, method, receiverType, node));
    }

    return functions;
  }

  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();
    KotlinWriter.appendAnnotations(buf, indent, method.methodStruct, TypeAnnotation.METHOD_RETURN_TYPE);
    KotlinWriter.appendJvmAnnotations(buf, indent, method.methodStruct, false, method.classStruct.getPool(), TypeAnnotation.METHOD_RETURN_TYPE);

    buf.appendIndent(indent);

    if (flags.visibility != ProtoBuf.Visibility.PUBLIC || "1".equals(KotlinPreferences.getPreference(KotlinPreferences.SHOW_PUBLIC_VISIBILITY))) {
      KUtils.appendVisibility(buf, flags.visibility);
    }

    if (flags.isExpect) {
      buf.append("expect ");
    }

    if (flags.modality != ProtoBuf.Modality.FINAL) {
      buf.append(flags.modality.name().toLowerCase())
        .append(' ');
    }

    if (flags.isExternal) {
      buf.append("external ");
    }

    if (flags.isTailrec) {
      buf.append("tailrec ");
    }

    if (flags.isSuspend) {
      buf.append("suspend ");
    }

    if (flags.isInline) {
      buf.append("inline ");
    }

    if (flags.isInfix) {
      buf.append("infix ");
    }

    if (flags.isOperator) {
      buf.append("operator ");
    }

    buf.append("fun ");

    List<KTypeParameter> complexTypeParams = typeParameters.stream()
      .filter(typeParameter -> typeParameter.upperBounds.size() > 1)
      .collect(Collectors.toList());

    Map<Integer, KTypeParameter> typeParamsById = typeParameters.stream()
      .collect(Collectors.toMap(typeParameter -> typeParameter.id, Function.identity()));

    if (!typeParameters.isEmpty()) {
      MetadataNameResolver resolver = KotlinDecompilationContext.getNameResolver();
      buf.append('<');

      for (int i = 0; i < typeParameters.size(); i++) {
        KTypeParameter typeParameter = typeParameters.get(i);

        if (typeParameter.reified) {
          buf.append("reified ");
        }

        buf.append(KotlinWriter.toValidKotlinIdentifier(typeParameter.name));

        if (typeParameter.upperBounds.size() == 1) {
          buf.append(" : ").append(typeParameter.upperBounds.get(0).stringify(indent + 1));
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
        for (KType upperBound : typeParameter.upperBounds) {
          if (!first) {
            buf.appendPossibleNewline(",").appendPossibleNewline(" ");
          }

          buf.append(KotlinWriter.toValidKotlinIdentifier(typeParameter.name))
            .append(" : ")
            .append(upperBound.stringify(indent + 1));

          first = false;
        }
      }

      buf.appendPossibleNewline(" ", true)
        .popNewlineGroup();
    }

    KotlinWriter.writeMethodBody(node, method, buf, indent, false);

    return buf;
  }
}
