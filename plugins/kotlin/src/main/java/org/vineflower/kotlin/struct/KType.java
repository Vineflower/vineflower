package org.vineflower.kotlin.struct;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.KTypes;

public class KType extends VarType {
  public final String kotlinType;

  public final boolean isNullable;

  public final TypeArgument @Nullable [] typeArguments;

  public KType(VarType type, String kotlinType, boolean isNullable, TypeArgument @Nullable [] typeArguments) {
    super(type.type, type.arrayDim, type.value, type.typeFamily, type.stackSize, type.falseBoolean);
    this.kotlinType = kotlinType;
    this.isNullable = isNullable;
    this.typeArguments = typeArguments;
  }
  
  public static KType from(ProtoBuf.Type type, MetadataNameResolver nameResolver) {
    String kotlinType = nameResolver.resolve(type.getClassName());
    boolean isNullable = type.getNullable();
    String jvmDesc = KTypes.getJavaSignature(kotlinType, isNullable);
    VarType varType = new VarType(jvmDesc);

    TypeArgument[] typeArguments = null;
    if (type.getArgumentCount() > 0) {
      typeArguments = new TypeArgument[type.getArgumentCount()];
      for (int i = 0; i < type.getArgumentCount(); i++) {
        ProtoBuf.Type.Argument argument = type.getArgument(i);
        typeArguments[i] = new TypeArgument(from(argument.getType(), nameResolver), argument.getProjection());
      }
    }

    return new KType(varType, kotlinType, isNullable, typeArguments);
  }

  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();

    if (!kotlinType.startsWith("kotlin/Function")) { // Non-functions are essentially equivalent to Java generic types
      buf.append(KTypes.getKotlinType(this));

      if (typeArguments != null) {
        buf.append("<");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        boolean first = true;
        for (TypeArgument typeArgument : typeArguments) {
          if (!first) {
            buf.append(",")
              .appendPossibleNewline(" ");
          }
          buf.append(typeArgument.stringify(indent + 1));
          first = false;
        }
        buf.appendPossibleNewline("", true);
        buf.append(">");
        buf.popNewlineGroup();

      }

      if (isNullable) {
        buf.append("?");
      }
    } else { // Metadata-defined function types always have all param types listed instead of defaulting to `FunctionN`
      buf.append(isNullable ? "((" : "(");
      buf.pushNewlineGroup(indent, 1);
      buf.appendPossibleNewline();
      for (int i = 0; i < typeArguments.length - 1; i++) {
        if (i != 0) {
          buf.append(",").appendPossibleNewline(" ");
        }
        buf.append(typeArguments[i].stringify(indent + 1));
      }
      buf.appendPossibleNewline("", true);
      buf.popNewlineGroup();
      buf.append(") -> ");
      buf.append(typeArguments[typeArguments.length - 1].stringify(indent + 1));
      if (isNullable) {
        buf.append(")?");
      }
    }

    return buf;
  }

  public static class TypeArgument {
    public final KType type;
    public final ProtoBuf.Type.Argument.Projection typeProjection;

    public TypeArgument(KType type, ProtoBuf.Type.Argument.Projection typeProjection) {
      this.type = type;
      this.typeProjection = typeProjection;
    }
    
    public TextBuffer stringify(int indent) {
      TextBuffer buf = new TextBuffer();
      switch (typeProjection) {
        case IN:
          buf.append("in ");
          break;
        case OUT:
          buf.append("out ");
          break;
        case STAR:
          buf.append("*");
          return buf;
        case INV:
          break;
      }
      buf.append(type.stringify(indent));
      return buf;
    }
  }
}
