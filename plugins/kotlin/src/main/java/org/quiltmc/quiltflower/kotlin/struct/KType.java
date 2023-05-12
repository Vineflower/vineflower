package org.quiltmc.quiltflower.kotlin.struct;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.quiltmc.quiltflower.kotlin.metadata.MetadataNameResolver;
import org.quiltmc.quiltflower.kotlin.util.KTypes;

public class KType {
  public final VarType type;

  public final String kotlinType;

  public final boolean isNullable;

  public final TypeArgument @Nullable [] typeArguments;

  public KType(VarType type, String kotlinType, boolean isNullable, TypeArgument @Nullable [] typeArguments) {
    this.type = type;
    this.kotlinType = kotlinType;
    this.isNullable = isNullable;
    this.typeArguments = typeArguments;
  }
  
  public static KType from(ProtoBuf.Type type, MetadataNameResolver nameResolver) {
    String kotlinType = nameResolver.resolve(type.getClassName());
    boolean isNullable = type.getNullable();
    String jvmDesc = KTypes.getJavaSignature(kotlinType, isNullable);
    VarType varType = new VarType(jvmDesc);

    TypeArgument[] typeArguments;
    if (type.getArgumentCount() > 0) {
      typeArguments = new TypeArgument[type.getArgumentCount()];
      for (int i = 0; i < type.getArgumentCount(); i++) {
        ProtoBuf.Type.Argument argument = type.getArgument(i);
        typeArguments[i] = new TypeArgument(from(argument.getType(), nameResolver), argument.getProjection());
      }
    } else {
      typeArguments = null;
    }

    return new KType(varType, kotlinType, isNullable, typeArguments);
  }

  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();

    if (!kotlinType.startsWith("kotlin/Function")) {
      buf.append(KTypes.getKotlinType(type));

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
    } else {
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
