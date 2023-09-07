package org.vineflower.kotlin.struct;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import kotlin.reflect.jvm.internal.impl.metadata.jvm.JvmProtoBuf;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
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
import org.vineflower.kotlin.KotlinDecompilationContext;
import org.vineflower.kotlin.KotlinPreferences;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.KUtils;
import org.vineflower.kotlin.util.ProtobufFlags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KConstructor {
  private static final VarType DEFAULT_CONSTRUCTOR_MARKER = new VarType("kotlin/jvm/internal/DefaultConstructorMarker", true);

  public final ProtobufFlags.Constructor flags;
  public final KParameter[] parameters;
  public final boolean isPrimary;

  public final MethodWrapper method;
  private final ClassesProcessor.ClassNode node;

  private KConstructor(
    KParameter[] parameters,
    ProtobufFlags.Constructor flags,
    MethodWrapper method,
    boolean isPrimary,
    ClassesProcessor.ClassNode node) {
    this.parameters = parameters;
    this.flags = flags;
    this.method = method;
    this.isPrimary = isPrimary;
    this.node = node;
  }

  public static Data parse(ClassesProcessor.ClassNode node) {
    MetadataNameResolver resolver = KotlinDecompilationContext.getNameResolver();
    ClassWrapper wrapper = node.getWrapper();
    StructClass struct = wrapper.getClassStruct();

    KotlinDecompilationContext.KotlinType type = KotlinDecompilationContext.getCurrentType();
    if (type != KotlinDecompilationContext.KotlinType.CLASS) return null;

    ProtobufFlags.Class classFlags = new ProtobufFlags.Class(KotlinDecompilationContext.getCurrentClass().getFlags());
    if (classFlags.modality == ProtoBuf.Modality.ABSTRACT) return null;

    List<ProtoBuf.Constructor> protoConstructors = KotlinDecompilationContext.getCurrentClass().getConstructorList();
    if (protoConstructors.isEmpty()) return null;

    Map<StructMethod, KConstructor> constructors = new HashMap<>();
    KConstructor primary = null;

    for (ProtoBuf.Constructor constructor : protoConstructors) {
      KParameter[] parameters = new KParameter[constructor.getValueParameterCount()];
      for (int i = 0; i < parameters.length; i++) {
        ProtoBuf.ValueParameter protoParameter = constructor.getValueParameter(i);
        parameters[i] = new KParameter(
          new ProtobufFlags.ValueParameter(protoParameter.getFlags()),
          resolver.resolve(protoParameter.getName()),
          KType.from(protoParameter.getType(), resolver),
          KType.from(protoParameter.getVarargElementType(), resolver),
          protoParameter.getTypeId()
        );
      }

      ProtobufFlags.Constructor flags = new ProtobufFlags.Constructor(constructor.getFlags());

      JvmProtoBuf.JvmMethodSignature signature = constructor.getExtension(JvmProtoBuf.constructorSignature);
      String desc = resolver.resolve(signature.getDesc());
      MethodWrapper method = wrapper.getMethodWrapper("<init>", desc);
      if (method == null) {
        DecompilerContext.getLogger().writeMessage("Method <init>" + desc + " not found in " + struct.qualifiedName, IFernflowerLogger.Severity.WARN);
        continue;
      }

      boolean isPrimary = !flags.isSecondary;

      KConstructor kConstructor = new KConstructor(parameters, flags, method, isPrimary, node);
      constructors.put(method.methodStruct, kConstructor);

      if (isPrimary) {
        primary = kConstructor;
      }
    }

    return new Data(constructors, primary);
  }

  public boolean stringify(TextBuffer buffer, int indent) {
    if (KotlinWriter.hideConstructor(node, true, false, parameters.length, method.methodStruct.getAccessFlags())) {
      return false;
    }

    TextBuffer buf = new TextBuffer();
    RootStatement root = method.root;

    if (!isPrimary) {
      boolean appended = false;

      if (flags.hasAnnotations) {
        KotlinWriter.appendAnnotations(buf, indent, method.methodStruct, TypeAnnotation.METHOD_RETURN_TYPE);
        KotlinWriter.appendJvmAnnotations(buf, indent, method.methodStruct, false, method.classStruct.getPool(), TypeAnnotation.METHOD_RETURN_TYPE);
        appended = true;
      }

      if (flags.visibility != ProtoBuf.Visibility.PUBLIC || "1".equals(KotlinPreferences.getPreference(KotlinPreferences.SHOW_PUBLIC_VISIBILITY))) {
        KUtils.appendVisibility(buf, flags.visibility);
        appended = true;
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
      }

      buf.appendPossibleNewline("", true).popNewlineGroup().append(") : ");

      Exprent firstExpr = root.getFirst().getExprents().get(0);
      if (!(firstExpr instanceof InvocationExprent)) {
        throw new IllegalStateException("First expression of constructor is not InvocationExprent");
      }

      InvocationExprent invocation = (InvocationExprent) firstExpr;
      buf.append(invocation.toJava(indent + 1), node.classStruct.qualifiedName, InterpreterUtil.makeUniqueKey(method.methodStruct.getName(), method.methodStruct.getDescriptor()));

      root.getFirst().getExprents().remove(0);
    }

    if (root.getFirst().getExprents().isEmpty()) {
      // There is no extra body so all done!
      buffer.append(buf);
      return true;
    }

    if (isPrimary) {
      buf.appendIndent(indent).append("init");
    }

    buf.append(" {").appendLineSeparator();

    TextBuffer body = root.toJava(indent + 1);
    body.addBytecodeMapping(root.getDummyExit().bytecode);

    StructMethod mt = method.methodStruct;
    buf.append(body, node.classStruct.qualifiedName, InterpreterUtil.makeUniqueKey(mt.getName(), mt.getDescriptor()));

    buf.appendIndent(indent).append("}\n");

    buffer.append(buf);
    return true;
  }

  public boolean writePrimaryConstructor(TextBuffer buffer, int indent) {
    if (!isPrimary) return false;

    TextBuffer buf = new TextBuffer();
    boolean appended = false;

    if (flags.hasAnnotations) {
      buf.append(" ");
      // -1 for indent indicates inline
      KotlinWriter.appendAnnotations(buf, -1, method.methodStruct, TypeAnnotation.METHOD_RETURN_TYPE);
      KotlinWriter.appendJvmAnnotations(buf, -1, method.methodStruct, false, method.classStruct.getPool(), TypeAnnotation.METHOD_RETURN_TYPE);
      appended = true;
    }

    // For cleanliness, public primary constructors are not forced public by the config option
    if (flags.visibility != ProtoBuf.Visibility.PUBLIC || (appended && "1".equals(KotlinPreferences.getPreference(KotlinPreferences.SHOW_PUBLIC_VISIBILITY)))) {
      buf.append(" ");
      KUtils.appendVisibility(buf, flags.visibility);
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
      }

      buf.appendPossibleNewline("", true).popNewlineGroup().append(")");
    }

    RootStatement root = method.root;
    if (root.getFirst().getExprents().isEmpty()) {
      // No ability to declare super constructor call
      buffer.append(buf);
      return false;
    }

    Exprent firstExpr = root.getFirst().getExprents().get(0);
    if (!(firstExpr instanceof InvocationExprent) || !((InvocationExprent) firstExpr).getName().equals("<init>")) {
      // no detected super constructor call
      buffer.append(buf);
      return false;
//      throw new IllegalStateException("First expression of constructor is not InvocationExprent");
    }

    InvocationExprent invocation = (InvocationExprent) firstExpr;
    if (invocation.getClassname().equals("java/lang/Object")) {
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

    root.getFirst().getExprents().remove(0);

    buffer.append(buf, node.classStruct.qualifiedName, InterpreterUtil.makeUniqueKey(method.methodStruct.getName(), method.methodStruct.getDescriptor()));
    return true;
  }

  public static class Data {
    public final Map<StructMethod, KConstructor> constructors;
    public final KConstructor primary;

    public Data(Map<StructMethod, KConstructor> constructors, KConstructor primary) {
      this.constructors = constructors;
      this.primary = primary;
    }
  }
}
