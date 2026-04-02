package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.util.Key;
import org.vineflower.kotlin.expr.KConstExprent;
import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kt.metadata.deserialization.Flags;
import org.vineflower.kt.metadata.jvm.JvmProtoBuf;
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
import org.jetbrains.java.decompiler.util.token.TokenType;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.KTypes;
import org.vineflower.kotlin.util.KUtils;

import java.util.ArrayList;
import java.util.List;
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
) implements KElement, Flags {
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
          .appendComment("//").appendWhitespace(" ").appendComment("$VF: failed to identify property annotations")
          .appendLineSeparator();
      }
    }

    buf.appendIndent(indent);

    // Modifiers in the order that Kotlin's coding conventions specify
    KUtils.appendVisibility(buf, VISIBILITY.get(flags));

    if (IS_EXPECT_PROPERTY.get(flags)) {
      buf.appendKeyword("expect").appendWhitespace(" ");
    }

    if (MODALITY.get(flags) == ProtoBuf.Modality.FINAL) {
      buf.appendKeyword(IS_CONST.get(flags) ? "const" : "final").appendWhitespace(" ");
    } else if (!classStruct.hasModifier(CodeConstants.ACC_INTERFACE) || MODALITY.get(flags) != ProtoBuf.Modality.ABSTRACT) {
      buf.appendKeyword(MODALITY.get(flags).name().toLowerCase())
        .appendWhitespace(" ");
    }

    if (IS_EXTERNAL_PROPERTY.get(flags)) {
      buf.appendKeyword("external").appendWhitespace(" ");
    }

    if (IS_LATEINIT.get(flags)) {
      buf.appendKeyword("lateinit").appendWhitespace(" ");
    }

    buf.appendKeyword(IS_VAR.get(flags) ? "var" : "val").appendWhitespace(" ")
      .appendField(KotlinWriter.toValidKotlinIdentifier(name), true, classStruct.qualifiedName, name, type.kotlinType)
      .appendPunctuation(":").appendWhitespace(" ")
      .append(type.stringify(indent)); 

    if (initializer != null) {
      Exprent expr = initializer.apply(classWrapper);
      if (expr != null) {
        TextBuffer initializerBuf = expr.toJava(indent);
        if (IS_DELEGATED.get(flags)) {
          buf.appendWhitespace(" ").appendKeyword("by").appendWhitespace(" ")
            .append(initializerBuf);
        } else {
          buf.appendWhitespace(" ").appendOperator("=")
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

      buf.appendKeyword(MODALITY.get(getter.flags()).name().toLowerCase())
        .appendWhitespace(" ");

      if (IS_EXTERNAL_ACCESSOR.get(getter.flags())) {
        buf.appendKeyword("external").appendWhitespace(" ");
      }

      if (IS_INLINE_ACCESSOR.get(getter.flags())) {
        buf.appendKeyword("inline").appendWhitespace(" ");
      }

      buf.appendKeyword("get").appendPunctuation("()").appendWhitespace(" ");

      KotlinWriter.writeMethodBody(classStruct, getter.methodSupplier().apply(classWrapper), buf, indent + 1, false);

      buf.popNewlineGroup();
    } else if (getter != null && IS_EXTERNAL_ACCESSOR.get(getter.flags())) {
      buf.appendLineSeparator()
        .appendIndent(indent + 1)
        .appendKeyword("external").appendWhitespace(" ").appendKeyword("get");
    }

    if (setter != null && IS_NOT_DEFAULT.get(setter.flags())) {
      buf.pushNewlineGroup(indent, 1)
        .appendLineSeparator()
        .appendIndent(indent + 1);

      KUtils.appendVisibility(buf, VISIBILITY.get(setter.flags()));

      buf.appendKeyword(MODALITY.get(setter.flags()).name().toLowerCase())
        .appendWhitespace(" ");

      if (IS_EXTERNAL_ACCESSOR.get(setter.flags())) {
        buf.appendKeyword("external").appendWhitespace(" ");
      }

      if (IS_INLINE_ACCESSOR.get(setter.flags())) {
        buf.appendKeyword("inline").appendWhitespace(" ");
      }

      buf.appendKeyword("set").appendPunctuation("(")
        .append(setterParamName, TokenType.PARAMETER)
        .appendPunctuation(")").appendWhitespace(" ");

      KotlinWriter.writeMethodBody(classStruct, setter.methodSupplier().apply(classWrapper), buf, indent + 1, false);

      buf.popNewlineGroup();
    } else if (setter != null && (IS_EXTERNAL_ACCESSOR.get(setter.flags()) || VISIBILITY.get(setter.flags()) != VISIBILITY.get(flags) || MODALITY.get(setter.flags()) != MODALITY.get(flags))) {
      buf.appendLineSeparator().appendIndent(indent + 1);

      if (VISIBILITY.get(setter.flags()) != VISIBILITY.get(flags)) {
        KUtils.appendVisibility(buf, VISIBILITY.get(setter.flags()));
      }

      if (MODALITY.get(setter.flags()) != MODALITY.get(flags)) {
        buf.appendKeyword(MODALITY.get(setter.flags()).name().toLowerCase())
          .appendWhitespace(" ");
      }

      if (IS_EXTERNAL_ACCESSOR.get(setter.flags())) {
        buf.appendKeyword("external").appendWhitespace(" ");
      }

      buf.appendKeyword("set");
    } else if (setter == null && IS_VAR.get(flags) && VISIBILITY.get(flags) != ProtoBuf.Visibility.PRIVATE) { // Special case: no setter is generated if it's a var with a private setter
      buf.appendLineSeparator()
        .appendIndent(indent + 1)
        .appendKeyword("private").appendWhitespace(" ").appendKeyword("set");
    }

    return buf;
  }

  public static void parse(StructClass classStruct, List<ProtoBuf.Property> protoProperties, @Nullable MetadataNameResolver nameResolver, StructClass companionParent) {
    if (nameResolver == null) {
      return;
    }

    for (ProtoBuf.Property property : protoProperties) {
      int flags = property.getFlags();

      JvmProtoBuf.JvmPropertySignature jvmProp = property.getExtension(JvmProtoBuf.propertySignature);

      List<AnnotationExprent> annotations = new ArrayList<>();
      if (jvmProp.hasSyntheticMethod()) {
        // Properties containing annotations receive a synthetic method which has the annotations in place of the property.
        // https://github.com/JetBrains/kotlin/blob/master/core/metadata.jvm/src/jvm_metadata.proto#L84
        JvmProtoBuf.JvmMethodSignature syntheticMethod = jvmProp.getSyntheticMethod();
        String methodName = nameResolver.resolve(syntheticMethod.getName());
        String desc = nameResolver.resolve(syntheticMethod.getDesc());
        StructMethod method = classStruct.getMethod(methodName, desc);
        if (method != null) {
          method.getAttributes().put(KElement.KEY, KHiddenElement.GENERIC);
          if (method.hasAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS)) {
            StructAnnotationAttribute attribute = method.getAttribute(StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS);
            annotations.addAll(attribute.getAnnotations());
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
          delegateField.getAttributes().put(KElement.KEY, KHiddenElement.GENERIC);
          String key = InterpreterUtil.makeUniqueKey(delegateFieldName, delegateDesc);
          if (delegateField.hasModifier(CodeConstants.ACC_STATIC)) {
            delegateSupplier = wrapper -> wrapper.getStaticFieldInitializers().getWithKey(key);
          } else {
            delegateSupplier = wrapper -> wrapper.getDynamicFieldInitializers().getWithKey(key);
          }
        }
      }

      KPropertyAccessor getter = null;
      StructMethod getterMethod = null;
      if (HAS_GETTER.get(flags)) {
        String methodName = nameResolver.resolve(jvmProp.getGetter().getName());
        String desc = nameResolver.resolve(jvmProp.getGetter().getDesc());
        getterMethod = classStruct.getMethod(methodName, desc);
        if (getterMethod != null) {
          Function<ClassWrapper, MethodWrapper> supplier = wrapper -> wrapper.getMethodWrapper(methodName, desc);
          getter = new KPropertyAccessor(property.getGetterFlags(), supplier, getterMethod);

          if (propDesc == null) {
            propDesc = getterMethod.getDescriptor().substring(getterMethod.getDescriptor().indexOf(')') + 1);
          }
        }

        if (companionParent != null) {
          StructMethod inParent = companionParent.getMethod(methodName, desc);
          if (inParent != null) {
            inParent.getAttributes().put(KElement.KEY, KHiddenElement.COMPANION_ITEM);
          }
        }
      }

      KPropertyAccessor setter = null;
      StructMethod setterMethod = null;
      String setterParamName = null;
      if (HAS_SETTER.get(flags)) {
        String methodName = nameResolver.resolve(jvmProp.getSetter().getName());
        String desc = nameResolver.resolve(jvmProp.getSetter().getDesc());
        setterMethod = classStruct.getMethod(methodName, desc);
        if (setterMethod != null) {
          Function<ClassWrapper, MethodWrapper> supplier = wrapper -> wrapper.getMethodWrapper(methodName, desc);
          setter = new KPropertyAccessor(property.getSetterFlags(), supplier, setterMethod);
          setterParamName = nameResolver.resolve(property.getSetterValueParameter().getName());
        }

        if (companionParent != null) {
          StructMethod inParent = companionParent.getMethod(methodName, desc);
          if (inParent != null) {
            inParent.getAttributes().put(KElement.KEY, KHiddenElement.COMPANION_ITEM);
          }
        }
      }

      StructField field = null;
      StructClass fieldContainer = classStruct;
      if (jvmProp.hasField()) {
        String fieldName = jvmProp.getField().hasName() ? nameResolver.resolve(jvmProp.getField().getName()) : name;
        String fieldDesc = jvmProp.getField().hasDesc() ? nameResolver.resolve(jvmProp.getField().getDesc()) : propDesc;

        if (fieldName != null && fieldDesc != null) {
          field = classStruct.getField(fieldName, fieldDesc);

          if (companionParent != null) {
            // Companion objects put fields in the parent class sometimes
            StructField inParent = companionParent.getField(fieldName, fieldDesc);
            if (inParent != null) {
              inParent.getAttributes().put(KElement.KEY, KHiddenElement.COMPANION_ITEM);

              if (field == null) {
                field = inParent;
                fieldContainer = companionParent;
              }
            }
          }
        } else {
          VBStyleCollection<StructField, String> fields = classStruct.getFields();
          for (StructField f : fields) {
            if (f.getName().equals(name)) {
              field = f;
              propDesc = f.getDescriptor();
              break;
            }
          }
        }
      }

      if (HAS_ANNOTATIONS.get(flags) && annotations.isEmpty() && field != null) {
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
        PrimitiveConstant constant = fieldContainer.getPool().getPrimitiveConstant(attr.getIndex());
        VarType constType = type != null ? type : varType;
        initializer = ignored -> new KConstExprent(new ConstExprent(constType, constant.value, null));
      } else if (field.hasModifier(CodeConstants.ACC_STATIC)) {
        initializer = wrapper -> wrapper.getStaticFieldInitializers().getWithKey(key);
      } else {
        initializer = wrapper -> wrapper.getDynamicFieldInitializers().getWithKey(key);
      }

      KProperty kprop = new KProperty(name, type, flags, getter, setter, setterParamName, field, initializer, annotations, classStruct);

      if (field != null) {
        field.getAttributes().put(KElement.KEY, kprop);
      }
      if (getterMethod != null) {
        getterMethod.getAttributes().put(KElement.KEY, kprop);
        getterMethod.getAttributes().put(PropertyMethod.KEY, PropertyMethod.GETTER);
      }
      if (setterMethod != null) {
        setterMethod.getAttributes().put(KElement.KEY, kprop);
        setterMethod.getAttributes().put(PropertyMethod.KEY, PropertyMethod.SETTER);
      }

      if (classStruct != fieldContainer) {
        // pretend the field is present in the class if it's a companion field in the parent class
        // why do you do this kotlin
        if (field != null) {
          classStruct.getFields().addWithKey(field, InterpreterUtil.makeUniqueKey(field.getName(), field.getDescriptor()));
        }
        if (getterMethod != null) {
          classStruct.getMethods().addWithKey(getterMethod, InterpreterUtil.makeUniqueKey(getterMethod.getName(), getterMethod.getDescriptor()));
        }
        if (setterMethod != null) {
          classStruct.getMethods().addWithKey(setterMethod, InterpreterUtil.makeUniqueKey(setterMethod.getName(), setterMethod.getDescriptor()));
        }
      }
    }
  }
  
  public enum PropertyMethod {
    GETTER,
    SETTER;
    
    public static final Key<PropertyMethod> KEY = Key.of(PropertyMethod.class.getName());
  }
}
