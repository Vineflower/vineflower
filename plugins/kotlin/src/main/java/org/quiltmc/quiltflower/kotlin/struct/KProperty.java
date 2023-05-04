package org.quiltmc.quiltflower.kotlin.struct;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import kotlin.reflect.jvm.internal.impl.metadata.jvm.JvmProtoBuf;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
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
  public final VarType type;

  public final ProtobufFlags.Property flags;

  @Nullable
  public final KPropertyAccessor getter;

  @Nullable
  public final KPropertyAccessor setter;

  @Nullable
  public final StructField underlyingField;

  @Nullable
  public final Exprent initializer;

  public KProperty(
    String name,
    VarType type,
    ProtobufFlags.Property flags,
    KPropertyAccessor getter,
    KPropertyAccessor setter,
    StructField underlyingField,
    Exprent initializer
  ) {
    this.name = name;
    this.type = type;
    this.flags = flags;
    this.getter = getter;
    this.setter = setter;
    this.underlyingField = underlyingField;
    this.initializer = initializer;
  }

  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();

    buf.appendIndent(indent);

    switch (flags.visibility) {
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
        buf.append(flags.visibility.name().toLowerCase())
          .append(' ');
    }

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
      .append(KTypes.getKotlinType(type));

    if (initializer != null) {
      TextBuffer initializerBuf = initializer.toJava(indent);
      initializerBuf.clearUnassignedBytecodeMappingData();
      buf.append(" =")
        .pushNewlineGroup(indent, 1)
        .appendPossibleNewline(" ")
        .append(initializerBuf)
        .popNewlineGroup();
    }

    //TODO: delegation, getters, and setters

    buf.appendLineSeparator();

    return buf;
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

      String propDesc = null;

      StructField field = null;

      if (jvmProp.hasField() && jvmProp.getField().hasName() && jvmProp.getField().hasDesc()) {
        String fieldName = nameResolver.resolve(jvmProp.getField().getName());
        propDesc = nameResolver.resolve(jvmProp.getField().getDesc());
        field = structClass.getField(fieldName, propDesc);
        if (field != null) {
          discoveredFields.add(fieldName);
        }
      }

      KPropertyAccessor getter = null;
      if (flags.hasGetter) {
        String methodName = nameResolver.resolve(jvmProp.getGetter().getName());
        String desc = nameResolver.resolve(jvmProp.getGetter().getDesc());
        StructMethod method = structClass.getMethod(methodName, desc);
        if (method != null) {
          getter = new KPropertyAccessor(new ProtobufFlags.PropertyAccessor(property.getFlags()), method);
          discoveredMethods.add(InterpreterUtil.makeUniqueKey(methodName, desc));

          if (propDesc == null) {
            propDesc = method.getDescriptor().substring(method.getDescriptor().indexOf(')') + 1);
          }
        }
      }

      KPropertyAccessor setter = null;
      if (flags.hasSetter) {
        String methodName = nameResolver.resolve(jvmProp.getSetter().getName());
        String desc = nameResolver.resolve(jvmProp.getSetter().getDesc());
        StructMethod method = structClass.getMethod(methodName, desc);
        if (method != null) {
          setter = new KPropertyAccessor(new ProtobufFlags.PropertyAccessor(property.getFlags()), method);
          discoveredMethods.add(InterpreterUtil.makeUniqueKey(methodName, desc));

          if (propDesc == null) {
            propDesc = method.getDescriptor().substring(method.getDescriptor().indexOf(')') + 1);
          }
        }
      }

      if (field == null && propDesc != null) {
        field = structClass.getField(name, propDesc);
        if (field != null) {
          discoveredFields.add(name);
        }
      } else if (field == null) {
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

      if (field == null) {
        initializer = null;
      } else if (flags.isConst && field.hasAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE)) {
        StructConstantValueAttribute attr = field.getAttribute(StructGeneralAttribute.ATTRIBUTE_CONSTANT_VALUE);
        PrimitiveConstant constant = structClass.getPool().getPrimitiveConstant(attr.getIndex());
        initializer = new ConstExprent(varType, constant.value, null);
      } else if (field.hasModifier(CodeConstants.ACC_STATIC)) {
        initializer = wrapper.getStaticFieldInitializers().getWithKey(key);
      } else {
        initializer = wrapper.getDynamicFieldInitializers().getWithKey(key);
      }

      list.add(new KProperty(name, varType, flags, getter, setter, field, initializer));
    }

    return true;
  }
}
