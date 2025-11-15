package org.vineflower.kotlin.struct;

import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kt.metadata.deserialization.Flags;
import org.vineflower.kt.metadata.jvm.JvmProtoBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
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
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.metadata.StructKotlinMetadataAttribute;
import org.vineflower.kotlin.util.KTypes;
import org.vineflower.kotlin.util.KUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public record KProperty(
  String name,
  KType type,
  int flags,
  @Nullable KPropertyAccessor getter,
  @Nullable KPropertyAccessor setter,
  @Nullable String setterParamName,
  @Nullable StructField underlyingField,
  @Nullable Function<ClassWrapper, Exprent> initializer,
  @Nullable List<AnnotationExprent> annotations,
  StructClass classStruct
) implements Flags {
  private static final AnnotationExprent DEPRECATED_ANNOTATION = new AnnotationExprent(
    new VarType("kotlin/Deprecated").value,
    List.of("message"),
    List.of(new ConstExprent(VarType.VARTYPE_STRING, "Deprecated by attribute.", null))
  );

  public TextBuffer stringify(ClassWrapper classWrapper, int indent) {
    TextBuffer buf = new TextBuffer();

    if (HAS_ANNOTATIONS.get(flags)) {
      if (annotations != null) {
        for (AnnotationExprent anno : annotations) {
          buf.appendIndent(indent)
            .append(anno.toJava(indent))
            .appendLineSeparator();
        }
      } else {
        buf.appendIndent(indent)
          .append("// $VF: failed to identify property annotations")
          .appendLineSeparator();
      }
    }

    buf.appendIndent(indent);

    // Modifiers in the order that Kotlin's coding conventions specify
    KUtils.appendVisibility(buf, VISIBILITY.get(flags));

    if (IS_EXPECT_PROPERTY.get(flags)) {
      buf.append("expect ");
    }

    if (MODALITY.get(flags) == ProtoBuf.Modality.FINAL) {
      buf.append(IS_CONST.get(flags) ? "const " : "final ");
    } else if (!classStruct.hasModifier(CodeConstants.ACC_INTERFACE) || MODALITY.get(flags) != ProtoBuf.Modality.ABSTRACT) {
      buf.append(MODALITY.get(flags).name().toLowerCase())
        .append(' ');
    }

    if (IS_EXTERNAL_PROPERTY.get(flags)) {
      buf.append("external ");
    }

    if (IS_LATEINIT.get(flags)) {
      buf.append("lateinit ");
    }

    buf.append(IS_VAR.get(flags) ? "var " : "val ")
      .append(KotlinWriter.toValidKotlinIdentifier(name))
      .append(": ")
      .append(type.stringify(indent)); 

    if (initializer != null) {
      Exprent expr = initializer.apply(classWrapper);
      if (expr != null) {
        TextBuffer initializerBuf = expr.toJava(indent);
        if (IS_DELEGATED.get(flags)) {
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
    }

    // Custom getters and setters, and possible modifier differences
    if (getter != null && IS_NOT_DEFAULT.get(getter.flags())) {
      buf.pushNewlineGroup(indent, 1)
          .appendLineSeparator()
          .appendIndent(indent + 1);

      KUtils.appendVisibility(buf, VISIBILITY.get(getter.flags()));

      buf.append(MODALITY.get(getter.flags()).name().toLowerCase())
        .append(' ');

      if (IS_EXTERNAL_ACCESSOR.get(getter.flags())) {
        buf.append("external ");
      }

      if (IS_INLINE_ACCESSOR.get(getter.flags())) {
        buf.append("inline ");
      }

      buf.append("get() ");

      KotlinWriter.writeMethodBody(classStruct, getter.methodSupplier().apply(classWrapper), buf, indent + 1, false);

      buf.popNewlineGroup();
    } else if (getter != null && IS_EXTERNAL_ACCESSOR.get(getter.flags())) {
      buf.appendLineSeparator()
        .appendIndent(indent + 1)
        .append("external get");
    }

    if (setter != null && IS_NOT_DEFAULT.get(setter.flags())) {
      buf.pushNewlineGroup(indent, 1)
        .appendLineSeparator()
        .appendIndent(indent + 1);

      KUtils.appendVisibility(buf, VISIBILITY.get(setter.flags()));

      buf.append(MODALITY.get(setter.flags()).name().toLowerCase())
        .append(' ');

      if (IS_EXTERNAL_ACCESSOR.get(setter.flags())) {
        buf.append("external ");
      }

      if (IS_INLINE_ACCESSOR.get(setter.flags())) {
        buf.append("inline ");
      }

      buf.append("set(")
        .append(setterParamName)
        .append(") ");

      KotlinWriter.writeMethodBody(classStruct, setter.methodSupplier().apply(classWrapper), buf, indent + 1, false);

      buf.popNewlineGroup();
    } else if (setter != null && (IS_EXTERNAL_ACCESSOR.get(setter.flags()) || VISIBILITY.get(setter.flags()) != VISIBILITY.get(flags) || MODALITY.get(setter.flags()) != MODALITY.get(flags))) {
      buf.appendLineSeparator().appendIndent(indent + 1);

      if (VISIBILITY.get(setter.flags()) != VISIBILITY.get(flags)) {
        KUtils.appendVisibility(buf, VISIBILITY.get(setter.flags()));
      }

      if (MODALITY.get(setter.flags()) != MODALITY.get(flags)) {
        buf.append(MODALITY.get(setter.flags()).name().toLowerCase())
          .append(' ');
      }

      if (IS_EXTERNAL_ACCESSOR.get(setter.flags())) {
        buf.append("external ");
      }

      buf.append("set");
    } else if (setter == null && IS_VAR.get(flags) && VISIBILITY.get(flags) != ProtoBuf.Visibility.PRIVATE) { // Special case: no setter is generated if it's a var with a private setter
      buf.appendLineSeparator()
        .appendIndent(indent + 1)
        .append("private set");
    }

    return buf;
  }

  public static @Nullable Data parse(StructClass classStruct) {
    StructKotlinMetadataAttribute ktData = classStruct.getAttribute(StructKotlinMetadataAttribute.KEY);
    if (ktData == null || ktData.nameResolver == null) {
      return null;
    }

    MetadataNameResolver nameResolver = ktData.nameResolver;

    List<ProtoBuf.Property> protoProperties;

    if (ktData.metadata instanceof StructKotlinMetadataAttribute.Class cls) {
      protoProperties = cls.proto().getPropertyList();
    } else if (ktData.metadata instanceof StructKotlinMetadataAttribute.File file) {
      protoProperties = file.proto().getPropertyList();
    } else if (ktData.metadata instanceof StructKotlinMetadataAttribute.MultifileClass multifileClass) {
      protoProperties = multifileClass.proto().getPropertyList();
    } else if (ktData.metadata instanceof StructKotlinMetadataAttribute.SyntheticClass) {
      return null;
    } else {
      throw new IllegalStateException("Impossible metadata value");
    }

    List<KProperty> properties = new ArrayList<>();
    Set<StructField> associatedFields = new HashSet<>();
    Set<StructMethod> associatedMethods = new HashSet<>();

    for (ProtoBuf.Property property : protoProperties) {
      int flags = property.getFlags();

      JvmProtoBuf.JvmPropertySignature jvmProp = property.getExtension(JvmProtoBuf.propertySignature);

      List<AnnotationExprent> annotations = new ArrayList<>();
      if (jvmProp.hasSyntheticMethod()) {
        // Properties containing annotations receive a synthetic methodSupplier which has the annotations in place of the property.
        // https://github.com/JetBrains/kotlin/blob/master/core/metadata.jvm/src/jvm_metadata.proto#L84
        JvmProtoBuf.JvmMethodSignature syntheticMethod = jvmProp.getSyntheticMethod();
        String methodName = nameResolver.resolve(syntheticMethod.getName());
        String desc = nameResolver.resolve(syntheticMethod.getDesc());
        StructMethod method = classStruct.getMethod(methodName, desc);
        if (method != null) {
          associatedMethods.add(method);
          if (method.hasAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS)) {
            StructAnnotationAttribute attribute = method.getAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS);
            annotations = attribute.getAnnotations();
          }
          if (method.hasAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS)) {
            StructAnnotationAttribute attribute = method.getAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS);
            annotations.addAll(attribute.getAnnotations());
          }
        }
      }

      String name = nameResolver.resolve(property.getName());

      String propDesc = null;
      KType type = null;
      if (property.hasReturnType()) {
        type = KType.from(property.getReturnType(), nameResolver);
        propDesc = KTypes.getJavaSignature(type.kotlinType, property.getReturnType().getNullable());
      }

      // Delegates create a hidden field containing the created delegate, so reference that instead
      Function<ClassWrapper, Exprent> delegateSupplier = null;
      StructField delegateField = null;
      if (IS_DELEGATED.get(flags)) {
        String delegateFieldName = nameResolver.resolve(jvmProp.getField().getName());
        String delegateDesc = nameResolver.resolve(jvmProp.getField().getDesc());
        delegateField = classStruct.getField(delegateFieldName, delegateDesc);
        if (delegateField != null) {
          associatedFields.add(delegateField);
          String key = InterpreterUtil.makeUniqueKey(delegateFieldName, delegateDesc);
          if (delegateField.hasModifier(CodeConstants.ACC_STATIC)) {
            delegateSupplier = wrapper -> wrapper.getStaticFieldInitializers().getWithKey(key);
          } else {
            delegateSupplier = wrapper -> wrapper.getDynamicFieldInitializers().getWithKey(key);
          }
        }
      }

      KPropertyAccessor getter = null;
      if (HAS_GETTER.get(flags)) {
        String methodName = nameResolver.resolve(jvmProp.getGetter().getName());
        String desc = nameResolver.resolve(jvmProp.getGetter().getDesc());
        StructMethod method = classStruct.getMethod(methodName, desc);
        if (method != null) {
          Function<ClassWrapper, MethodWrapper> supplier = wrapper -> wrapper.getMethodWrapper(methodName, desc);
          getter = new KPropertyAccessor(property.getGetterFlags(), supplier);
          associatedMethods.add(method);

          if (propDesc == null) {
            propDesc = method.getDescriptor().substring(method.getDescriptor().indexOf(')') + 1);
          }
        }
      }

      KPropertyAccessor setter = null;
      String setterParamName = null;
      if (HAS_SETTER.get(flags)) {
        String methodName = nameResolver.resolve(jvmProp.getSetter().getName());
        String desc = nameResolver.resolve(jvmProp.getSetter().getDesc());
        StructMethod method = classStruct.getMethod(methodName, desc);
        if (method != null) {
          Function<ClassWrapper, MethodWrapper> supplier = wrapper -> wrapper.getMethodWrapper(methodName, desc);
          setter = new KPropertyAccessor(property.getSetterFlags(), supplier);
          associatedMethods.add(method);
          setterParamName = nameResolver.resolve(property.getSetterValueParameter().getName());
        }
      }

      StructField field = null;
      if (propDesc != null) {
        field = classStruct.getField(name, propDesc);
        if (field != null) {
          associatedFields.add(field);
        }
      } else {
        VBStyleCollection<StructField, String> fields = classStruct.getFields();
        for (StructField f : fields) {
          if (f.getName().equals(name)) {
            field = f;
            propDesc = f.getDescriptor();
            associatedFields.add(field);
            break;
          }
        }
      }

      if (HAS_ANNOTATIONS.get(flags) && annotations == null && field != null) {
        annotations = new ArrayList<>();

        if (field.hasAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS)) {
          StructAnnotationAttribute attribute = field.getAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS);
          annotations.addAll(attribute.getAnnotations());
        }
        if (field.hasAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS)) {
          StructAnnotationAttribute attribute = field.getAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS);
          annotations.addAll(attribute.getAnnotations());
        }
        if (field.hasAttribute(StructGeneralAttribute.ATTRIBUTE_DEPRECATED)) {
          annotations.add(DEPRECATED_ANNOTATION);
        }
      }

      VarType varType = propDesc != null ? new VarType(propDesc) : VarType.VARTYPE_OBJECT;

      String key = InterpreterUtil.makeUniqueKey(name, varType.toString());
      Function<ClassWrapper, Exprent> initializer;

      if (delegateSupplier != null) {
        initializer = delegateSupplier;
        field = delegateField;
      } else if (field == null) {
        initializer = null;
      } else if (field.hasAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE)) {
        StructConstantValueAttribute attr = field.getAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE);
        PrimitiveConstant constant = classStruct.getPool().getPrimitiveConstant(attr.getIndex());
        initializer = ignored -> new ConstExprent(varType, constant.value, null);
      } else if (field.hasModifier(CodeConstants.ACC_STATIC)) {
        initializer = wrapper -> wrapper.getStaticFieldInitializers().getWithKey(key);
      } else {
        initializer = wrapper -> wrapper.getDynamicFieldInitializers().getWithKey(key);
      }

      properties.add(new KProperty(name, type, flags, getter, setter, setterParamName, field, initializer, annotations, classStruct));
    }

    return new Data(properties, associatedFields, associatedMethods);
  }

  public record Data(
    @NotNull List<KProperty> properties,
    @NotNull Set<StructField> associatedFields,
    @NotNull Set<StructMethod> associatedMethods
  ) { }
}
