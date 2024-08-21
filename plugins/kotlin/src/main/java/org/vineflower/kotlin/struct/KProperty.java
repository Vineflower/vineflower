package org.vineflower.kotlin.struct;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import kotlinx.metadata.internal.metadata.jvm.JvmProtoBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructConstantValueAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;
import org.vineflower.kotlin.KotlinDecompilationContext;
import org.vineflower.kotlin.KotlinOptions;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.KTypes;
import org.vineflower.kotlin.util.KUtils;
import org.vineflower.kotlin.util.ProtobufFlags;

import java.util.*;

public record KProperty(
  String name,
  KType type,
  ProtobufFlags.Property flags,
  @Nullable KPropertyAccessor getter,
  @Nullable KPropertyAccessor setter,
  @Nullable String setterParamName,
  @Nullable StructField underlyingField,
  @Nullable Exprent initializer,
  @Nullable StructAnnotationAttribute annotations,
  ClassesProcessor.ClassNode node
) {
  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();

    if (flags.hasAnnotations) {
      assert annotations != null;
      for (AnnotationExprent anno : annotations.getAnnotations()) {
        buf.appendIndent(indent)
          .append(anno.toJava(indent))
          .appendLineSeparator();
      }
    }

    buf.appendIndent(indent);

    // Modifiers in the order that Kotlin's coding conventions specify
    KUtils.appendVisibility(buf, flags.visibility);

    if (flags.isExpect) {
      buf.append("expect ");
    }

    if (Objects.requireNonNull(flags.modality) == ProtoBuf.Modality.FINAL) {
      buf.append(flags.isConst ? "const " : "final ");
    } else if (!node.classStruct.hasModifier(CodeConstants.ACC_INTERFACE) || flags.modality != ProtoBuf.Modality.ABSTRACT) {
      buf.append(flags.modality.name().toLowerCase())
        .append(' ');
    }

    if (flags.isExternal) {
      buf.append("external ");
    }

    if (flags.isLateinit) {
      buf.append("lateinit ");
    }

    buf.append(flags.isVar ? "var " : "val ")
      .append(KotlinWriter.toValidKotlinIdentifier(name))
      .append(": ")
      .append(type.stringify(indent)); 

    if (initializer != null) {
      TextBuffer initializerBuf = initializer.toJava(indent);
      initializerBuf.clearUnassignedBytecodeMappingData();
      if (flags.isDelegated) {
        buf.append(" by ")
          .append(initializerBuf);
      } else {
        buf.append(" =")
          .pushNewlineGroup(indent, 1)
          .appendPossibleNewline(" ")
          .append(initializerBuf)
          .popNewlineGroup();
      }
    }

    // Custom getters and setters, and possible modifier differences
    if (getter != null && getter.flags().isNotDefault) {
      buf.pushNewlineGroup(indent, 1)
          .appendLineSeparator()
          .appendIndent(indent + 1);

      KUtils.appendVisibility(buf, getter.flags().visibility);

      buf.append(getter.flags().modality.name().toLowerCase())
        .append(' ');

      if (getter.flags().isExternal) {
        buf.append("external ");
      }

      if (getter.flags().isInline) {
        buf.append("inline ");
      }

      buf.append("get() ");

      KotlinWriter.writeMethodBody(node, getter.underlyingMethod(), buf, indent + 1, false);

      buf.popNewlineGroup();
    } else if (getter != null && getter.flags().isExternal) {
      buf.appendLineSeparator()
        .appendIndent(indent + 1)
        .append("external get");
    }

    if (setter != null && setter.flags().isNotDefault) {
      buf.pushNewlineGroup(indent, 1)
        .appendLineSeparator()
        .appendIndent(indent + 1);

      KUtils.appendVisibility(buf, getter.flags().visibility);

      buf.append(setter.flags().modality.name().toLowerCase())
        .append(' ');

      if (setter.flags().isExternal) {
        buf.append("external ");
      }

      if (setter.flags().isInline) {
        buf.append("inline ");
      }

      buf.append("set(")
        .append(setterParamName)
        .append(") ");

      KotlinWriter.writeMethodBody(node, setter.underlyingMethod(), buf, indent + 1, false);

      buf.popNewlineGroup();
    } else if (setter != null && (setter.flags().isExternal || setter.flags().visibility != flags.visibility || setter.flags().modality != flags.modality)) {
      buf.appendLineSeparator().appendIndent(indent + 1);

      if (setter.flags().visibility != flags.visibility) {
        KUtils.appendVisibility(buf, setter.flags().visibility);
      }

      if (setter.flags().modality != flags.modality) {
        buf.append(setter.flags().modality.name().toLowerCase())
          .append(' ');
      }

      if (setter.flags().isExternal) {
        buf.append("external ");
      }

      buf.append("set");
    } else if (setter == null && flags.isVar && flags.visibility != ProtoBuf.Visibility.PRIVATE) { // Special case: no setter is generated if it's a var with a private setter
      buf.appendLineSeparator()
        .appendIndent(indent + 1)
        .append("private set");
    }

    return buf;
  }

  private static void appendVisibility(TextBuffer buf, ProtoBuf.Visibility visibility) {
    switch (visibility) {
      case LOCAL -> buf.append("// QF: local property")
        .appendLineSeparator()
        .append("internal ");
      case PRIVATE_TO_THIS -> buf.append("private ");
      case PUBLIC -> {
        if (DecompilerContext.getOption(KotlinOptions.SHOW_PUBLIC_VISIBILITY)) {
          buf.append("public ");
        }
      }
      default -> buf.append(visibility.name().toLowerCase()).append(' ');
    }
  }

  public static @Nullable Data parse(ClassesProcessor.ClassNode node) {
    MetadataNameResolver nameResolver = KotlinDecompilationContext.getNameResolver();
    ClassWrapper wrapper = node.getWrapper();
    StructClass structClass = wrapper.getClassStruct();

    KotlinDecompilationContext.KotlinType currentType = KotlinDecompilationContext.getCurrentType();
    if (currentType == null) return null;

    List<ProtoBuf.Property> protoProperties = switch (currentType) {
      case CLASS -> KotlinDecompilationContext.getCurrentClass().getPropertyList();
      case FILE -> KotlinDecompilationContext.getFilePackage().getPropertyList();
      case MULTIFILE_CLASS -> KotlinDecompilationContext.getMultifilePackage().getPropertyList();
      case SYNTHETIC_CLASS -> null;
    };

    if (protoProperties == null) return null;

    List<KProperty> properties = new ArrayList<>();
    Set<StructField> associatedFields = new HashSet<>();
    Set<StructMethod> associatedMethods = new HashSet<>();

    for (ProtoBuf.Property property : protoProperties) {
      JvmProtoBuf.JvmPropertySignature jvmProp = property.getExtension(JvmProtoBuf.propertySignature);

      StructAnnotationAttribute annotations = null;
      if (jvmProp.hasSyntheticMethod()) {
        // Properties containing annotations receive a synthetic method which has the annotations in place of the property.
        // https://github.com/JetBrains/kotlin/blob/master/core/metadata.jvm/src/jvm_metadata.proto#L84
        JvmProtoBuf.JvmMethodSignature syntheticMethod = jvmProp.getSyntheticMethod();
        String methodName = nameResolver.resolve(syntheticMethod.getName());
        String desc = nameResolver.resolve(syntheticMethod.getDesc());
        StructMethod method = structClass.getMethod(methodName, desc);
        if (method != null) {
          associatedMethods.add(method);
          annotations = method.getAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS);
        }
      }

      ProtobufFlags.Property flags = new ProtobufFlags.Property(property.getFlags());

      String name = nameResolver.resolve(property.getName());

      String propDesc = null;
      KType type = null;
      if (property.hasReturnType()) {
        type = KType.from(property.getReturnType(), nameResolver);
        propDesc = KTypes.getJavaSignature(type.kotlinType, property.getReturnType().getNullable());
      }

      // Delegates create a hidden field containing the created delegate, so reference that instead
      Exprent delegateExprent = null;
      if (flags.isDelegated) {
        String delegateFieldName = nameResolver.resolve(jvmProp.getField().getName());
        String delegateDesc = nameResolver.resolve(jvmProp.getField().getDesc());
        StructField delegateField = structClass.getField(delegateFieldName, delegateDesc);
        if (delegateField != null) {
          associatedFields.add(delegateField);
          String key = InterpreterUtil.makeUniqueKey(delegateFieldName, delegateDesc);
          if (delegateField.hasModifier(CodeConstants.ACC_STATIC)) {
            delegateExprent = wrapper.getStaticFieldInitializers().getWithKey(key);
          } else {
            delegateExprent = wrapper.getDynamicFieldInitializers().getWithKey(key);
          }
        }
      }

      KPropertyAccessor getter = null;
      if (flags.hasGetter) {
        String methodName = nameResolver.resolve(jvmProp.getGetter().getName());
        String desc = nameResolver.resolve(jvmProp.getGetter().getDesc());
        StructMethod method = structClass.getMethod(methodName, desc);
        if (method != null) {
          MethodWrapper methodWrapper = wrapper.getMethodWrapper(methodName, desc);
          getter = new KPropertyAccessor(new ProtobufFlags.PropertyAccessor(property.getGetterFlags()), methodWrapper);
          associatedMethods.add(method);

          if (propDesc == null) {
            propDesc = method.getDescriptor().substring(method.getDescriptor().indexOf(')') + 1);
          }
        }
      }

      KPropertyAccessor setter = null;
      String setterParamName = null;
      if (flags.hasSetter) {
        String methodName = nameResolver.resolve(jvmProp.getSetter().getName());
        String desc = nameResolver.resolve(jvmProp.getSetter().getDesc());
        StructMethod method = structClass.getMethod(methodName, desc);
        if (method != null) {
          MethodWrapper methodWrapper = wrapper.getMethodWrapper(methodName, desc);
          setter = new KPropertyAccessor(new ProtobufFlags.PropertyAccessor(property.getSetterFlags()), methodWrapper);
          associatedMethods.add(method);
          setterParamName = nameResolver.resolve(property.getSetterValueParameter().getName());
        }
      }

      StructField field = null;
      if (propDesc != null) {
        field = structClass.getField(name, propDesc);
        if (field != null) {
          associatedFields.add(field);
        }
      } else {
        VBStyleCollection<StructField, String> fields = structClass.getFields();
        for (StructField f : fields) {
          if (f.getName().equals(name)) {
            field = f;
            propDesc = f.getDescriptor();
            associatedFields.add(field);
            break;
          }
        }
      }

      VarType varType = propDesc != null ? new VarType(propDesc) : VarType.VARTYPE_OBJECT;

      String key = InterpreterUtil.makeUniqueKey(name, varType.toString());
      Exprent initializer;

      if (delegateExprent != null) {
        initializer = delegateExprent;
      } else if (field == null) {
        initializer = null;
      } else if (field.hasAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE)) {
        StructConstantValueAttribute attr = field.getAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE);
        PrimitiveConstant constant = structClass.getPool().getPrimitiveConstant(attr.getIndex());
        initializer = new ConstExprent(varType, constant.value, null);
      } else if (field.hasModifier(CodeConstants.ACC_STATIC)) {
        initializer = wrapper.getStaticFieldInitializers().getWithKey(key);
      } else {
        initializer = wrapper.getDynamicFieldInitializers().getWithKey(key);
      }

      properties.add(new KProperty(name, type, flags, getter, setter, setterParamName, field, initializer, annotations, node));
    }

    return new Data(properties, associatedFields, associatedMethods);
  }

  public record Data(
    @NotNull List<KProperty> properties,
    @NotNull Set<StructField> associatedFields,
    @NotNull Set<StructMethod> associatedMethods
  ) { }
}
