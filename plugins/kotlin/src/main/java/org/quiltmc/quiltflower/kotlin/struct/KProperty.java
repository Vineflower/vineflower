package org.quiltmc.quiltflower.kotlin.struct;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import kotlin.reflect.jvm.internal.impl.metadata.jvm.JvmProtoBuf;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructConstantValueAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;
import org.quiltmc.quiltflower.kotlin.KotlinDecompilationContext;
import org.quiltmc.quiltflower.kotlin.KotlinPreferences;
import org.quiltmc.quiltflower.kotlin.KotlinWriter;
import org.quiltmc.quiltflower.kotlin.metadata.MetadataNameResolver;
import org.quiltmc.quiltflower.kotlin.util.KTypes;
import org.quiltmc.quiltflower.kotlin.util.ProtobufFlags;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class KProperty {
  public final String name;
  public final KType type;

  public final ProtobufFlags.Property flags;

  @Nullable
  public final KPropertyAccessor getter;

  @Nullable
  public final KPropertyAccessor setter;

  @Nullable
  public final String setterParamName;

  @Nullable
  public final StructField underlyingField;

  @Nullable
  public final Exprent initializer;

  private final ClassesProcessor.ClassNode node;

  public KProperty(
    String name,
    KType type,
    ProtobufFlags.Property flags,
    KPropertyAccessor getter,
    KPropertyAccessor setter,
    @Nullable String setterParamName, StructField underlyingField,
    Exprent initializer,
    ClassesProcessor.ClassNode node) {
    this.name = name;
    this.type = type;
    this.flags = flags;
    this.getter = getter;
    this.setter = setter;
    this.setterParamName = setterParamName;
    this.underlyingField = underlyingField;
    this.initializer = initializer;
    this.node = node;
  }

  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();

    buf.appendIndent(indent);

    appendVisibility(buf, flags.visibility);

    if (flags.isExpect) {
      buf.append("expect ");
    }

    if (Objects.requireNonNull(flags.modality) == ProtoBuf.Modality.FINAL) {
      buf.append(flags.isConst ? "const " : "final ");
    } else {
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

    if (getter != null && getter.flags.isNotDefault) {
      buf.pushNewlineGroup(indent, 1)
          .append('\n')
          .appendIndent(indent + 1);

      appendVisibility(buf, getter.flags.visibility);

      buf.append(getter.flags.modality.name().toLowerCase())
        .append(' ');

      if (getter.flags.isExternal) {
        buf.append("external ");
      }

      if (getter.flags.isInline) {
        buf.append("inline ");
      }

      buf.append("get() ");

      KotlinWriter.writeMethodBody(node, getter.underlyingMethod, buf, indent + 1, false);

      buf.popNewlineGroup();
    } else if (getter != null && getter.flags.isExternal) {
      buf.append('\n')
        .appendIndent(indent + 1)
        .append("external get");
    }

    if (setter != null && setter.flags.isNotDefault) {
      buf.pushNewlineGroup(indent, 1)
        .append('\n')
        .appendIndent(indent + 1);

      appendVisibility(buf, getter.flags.visibility);

      buf.append(setter.flags.modality.name().toLowerCase())
        .append(' ');

      if (setter.flags.isExternal) {
        buf.append("external ");
      }

      if (setter.flags.isInline) {
        buf.append("inline ");
      }

      buf.append("set(")
        .append(setterParamName)
        .append(") ");

      KotlinWriter.writeMethodBody(node, setter.underlyingMethod, buf, indent + 1, false);

      buf.popNewlineGroup();
    } else if (setter != null && (setter.flags.isExternal || setter.flags.visibility != flags.visibility || setter.flags.modality != flags.modality)) {
      buf.append('\n').appendIndent(indent + 1);

      if (setter.flags.visibility != flags.visibility) {
        appendVisibility(buf, setter.flags.visibility);
      }

      if (setter.flags.modality != flags.modality) {
        buf.append(setter.flags.modality.name().toLowerCase())
          .append(' ');
      }

      if (setter.flags.isExternal) {
        buf.append("external ");
      }

      buf.append("set");
    } else if (setter == null && flags.isVar && flags.visibility != ProtoBuf.Visibility.PRIVATE) {
      buf.append('\n')
        .appendIndent(indent + 1)
        .append("private set");
    }

    buf.appendLineSeparator();

    return buf;
  }

    

  private static void appendVisibility(TextBuffer buf, ProtoBuf.Visibility visibility) {
    switch (visibility) {
      case LOCAL:
        buf.append("// QF: local property")
          .appendLineSeparator()
          .append("internal ");
        break;
      case PRIVATE_TO_THIS:
        buf.append("private ");
        break;
      case PUBLIC:
        String showPublicVisibility = KotlinPreferences.getPreference(KotlinPreferences.SHOW_PUBLIC_VISIBILITY);
        if (Objects.equals(showPublicVisibility, "1")) {
          buf.append("public ");
        }
        break;
      default:
        buf.append(visibility.name().toLowerCase())
          .append(' ');
    }
  }

  public static boolean parse(ClassesProcessor.ClassNode node, List<KProperty> list, Set<String> discoveredFields, Set<String> discoveredMethods) {
    MetadataNameResolver nameResolver = KotlinDecompilationContext.getNameResolver();
    ClassWrapper wrapper = node.getWrapper();
    StructClass structClass = wrapper.getClassStruct();

    List<ProtoBuf.Property> properties;
    switch (KotlinDecompilationContext.getCurrentType()) {
      case CLASS:
        properties = KotlinDecompilationContext.getCurrentClass().getPropertyList();
        break;
      case FILE:
        properties = KotlinDecompilationContext.getFilePackage().getPropertyList();
        break;
      case MULTIFILE_CLASS:
        properties = KotlinDecompilationContext.getMultifilePackage().getPropertyList();
        break;
      case SYNTHETIC_CLASS:
        return false; // No property information in synthetic classes
      default:
        throw new IllegalStateException("Unexpected value: " + KotlinDecompilationContext.getCurrentType());
    }

    for (ProtoBuf.Property property : properties) {
      JvmProtoBuf.JvmPropertySignature jvmProp = property.getExtension(JvmProtoBuf.propertySignature);

      ProtobufFlags.Property flags = new ProtobufFlags.Property(property.getFlags());

      String name = nameResolver.resolve(property.getName());

      String propDesc;
      KType type;
      if (property.hasReturnType() && property.getReturnType().hasClassName()) {
        type = KType.from(property.getReturnType(), nameResolver);
        propDesc = KTypes.getJavaSignature(type.kotlinType, property.getReturnType().getNullable());
      } else {
        type = null;
        propDesc = null;
      }

      Exprent delegateExprent;
      if (flags.isDelegated) {
        String delegateFieldName = nameResolver.resolve(jvmProp.getField().getName());
        String delegateDesc = nameResolver.resolve(jvmProp.getField().getDesc());
        StructField delegateField = structClass.getField(delegateFieldName, delegateDesc);
        if (delegateField != null) {
          discoveredFields.add(delegateFieldName);
          String key = InterpreterUtil.makeUniqueKey(delegateFieldName, delegateDesc);
          if (delegateField.hasModifier(CodeConstants.ACC_STATIC)) {
            delegateExprent = wrapper.getStaticFieldInitializers().getWithKey(key);
          } else {
            delegateExprent = wrapper.getDynamicFieldInitializers().getWithKey(key);
          }
        } else {
          delegateExprent = null;
        }
      } else {
        delegateExprent = null;
      }

      KPropertyAccessor getter = null;
      if (flags.hasGetter) {
        String methodName = nameResolver.resolve(jvmProp.getGetter().getName());
        String desc = nameResolver.resolve(jvmProp.getGetter().getDesc());
        StructMethod method = structClass.getMethod(methodName, desc);
        if (method != null) {
          MethodWrapper methodWrapper = wrapper.getMethodWrapper(methodName, desc);
          getter = new KPropertyAccessor(new ProtobufFlags.PropertyAccessor(property.getGetterFlags()), methodWrapper);
          discoveredMethods.add(InterpreterUtil.makeUniqueKey(methodName, desc));

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
          discoveredMethods.add(InterpreterUtil.makeUniqueKey(methodName, desc));
          setterParamName = nameResolver.resolve(property.getSetterValueParameter().getName());
        }
      }

      StructField field = null;
      if (propDesc != null) {
        field = structClass.getField(name, propDesc);
        if (field != null) {
          discoveredFields.add(name);
        }
      } else {
        VBStyleCollection<StructField, String> fields = structClass.getFields();
        for (StructField f : fields) {
          if (f.getName().equals(name)) {
            field = f;
            propDesc = f.getDescriptor();
            discoveredFields.add(name);
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

      list.add(new KProperty(name, type, flags, getter, setter, setterParamName, field, initializer, node));
    }

    return true;
  }
}
