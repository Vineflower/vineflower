package org.vineflower.kotlin.struct;

import org.vineflower.kt.metadata.ProtoBuf;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.KTypes;

import java.util.Objects;

public class KType extends VarType {
  public static final KType UNIT = new KType(VARTYPE_VOID, "kotlin/Unit", false, false, null, null, null);
  public static final KType NOTHING = new KType(new VarType("java/lang/Void", true), "kotlin/Nothing", false, false, null, null, null);
  public static final KType UBYTE = new KType(VARTYPE_BYTE, "kotlin/UByte", false, true, null, null, null);
  public static final KType USHORT = new KType(VARTYPE_SHORT, "kotlin/UShort", false, true, null, null, null);
  public static final KType UINT = new KType(VARTYPE_INT, "kotlin/UInt", false, true, null, null, null);
  public static final KType ULONG = new KType(VARTYPE_LONG, "kotlin/ULong", false, true, null, null, null);

  public final String kotlinType;
  public final boolean isNullable;
  public final TypeArgument @Nullable [] typeArguments;
  public final boolean isUnsignedType;

  @Nullable
  public final String typeParameterName;

  @Nullable
  public final String typeAliasName;

  private KType(
    VarType type,
    String kotlinType,
    boolean isNullable,
    boolean isUnsignedType,
    TypeArgument @Nullable [] typeArguments,
    @Nullable String typeParameterName,
    @Nullable String typeAliasName) {
    super(type.type, type.arrayDim, type.value, type.typeFamily, type.stackSize);
    this.kotlinType = kotlinType;
    this.isNullable = isNullable;
    this.isUnsignedType = isUnsignedType;
    this.typeArguments = typeArguments;
    this.typeParameterName = typeParameterName;
    this.typeAliasName = typeAliasName;
  }

  public static KType from(ProtoBuf.Type type, MetadataNameResolver nameResolver) {
    String kotlinType = type.hasClassName() ? nameResolver.resolve(type.getClassName()) : "kotlin/Any";
    boolean isNullable = type.getNullable();

    // short-circuit for known types
    if (!isNullable) {
      KType existing = switch (kotlinType) {
        case "kotlin/Unit" -> UNIT;
        case "kotlin/Nothing" -> NOTHING;
        case "kotlin/UByte" -> UBYTE;
        case "kotlin/UShort" -> USHORT;
        case "kotlin/UInt" -> UINT;
        case "kotlin/ULong" -> ULONG;
        default -> null;
      };
      if (existing != null) {
        return existing;
      }
    }

    TypeArgument[] typeArguments = null;
    if (type.getArgumentCount() > 0) {
      typeArguments = new TypeArgument[type.getArgumentCount()];
      for (int i = 0; i < type.getArgumentCount(); i++) {
        ProtoBuf.Type.Argument argument = type.getArgument(i);
        typeArguments[i] = new TypeArgument(from(argument.getType(), nameResolver), argument.getProjection());
      }
    }

    String jvmDesc = KTypes.getJavaSignature(kotlinType, isNullable);
    if ("kotlin/Array".equals(kotlinType)) {
      TypeArgument arrayType = Objects.requireNonNull(typeArguments)[0];
      if (arrayType.typeProjection == ProtoBuf.Type.Argument.Projection.IN) {
        jvmDesc = "[Ljava/lang/Object;"; // `in` variance is erased to `Object` (to allow any supertype to be passed in)
      } else {
        jvmDesc = "[" + arrayType.type.toString();
      }
    }
    VarType varType = new VarType(jvmDesc);

    String typeParameterName = type.hasTypeParameterName() ? nameResolver.resolve(type.getTypeParameterName()) : null;
    String typeAliasName = type.hasTypeAliasName() ? nameResolver.resolve(type.getTypeAliasName()) : null;

    return new KType(varType, kotlinType, isNullable, false, typeArguments, typeParameterName, typeAliasName);
  }

  public static KType from(int tableIndex, MetadataNameResolver resolver, ProtoBuf.TypeTable typeTable) {
    return from(typeTable.getType(tableIndex), resolver);
  }

  // stringify is for the decompiler output
  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();

    if (typeParameterName != null) {
      buf.appendGeneric(typeParameterName, false, null, null, (String) null);
      if (isNullable) {
        buf.appendPunctuation('?');
      }

      return buf;
    }

    if (!kotlinType.startsWith("kotlin/Function")) { // Non-functions are essentially equivalent to Java generic types
      String type = kotlinType.replace('/', '.');
      buf.appendTypeName(DecompilerContext.getImportCollector().getShortName(type), this);

      if (typeArguments != null) {
        buf.appendPunctuation('<');
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        boolean first = true;
        for (TypeArgument typeArgument : typeArguments) {
          if (!first) {
            buf.appendPunctuation(",")
              .appendPossibleNewline(" ");
          }
          buf.append(typeArgument.stringify(indent + 1));
          first = false;
        }
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.appendPunctuation('>');

      }

      if (isNullable) {
        buf.appendPunctuation('?');
      }
    } else { // Metadata-defined function types always have all param types listed instead of defaulting to `FunctionN`
      buf.appendPunctuation(isNullable ? "((" : "(");
      buf.pushNewlineGroup(indent, 1);
      buf.appendPossibleNewline();
      for (int i = 0; i < typeArguments.length - 1; i++) {
        if (i != 0) {
          buf.appendPunctuation(",").appendPossibleNewline(" ");
        }
        buf.append(typeArguments[i].stringify(indent + 1));
      }
      buf.appendPossibleNewline("", true);
      buf.popNewlineGroup();
      buf.appendPunctuation(")").appendWhitespace(" ").appendOperator("->").appendWhitespace(" ");
      buf.append(typeArguments[typeArguments.length - 1].stringify(indent + 1));
      if (isNullable) {
        buf.appendPunctuation(")").appendPunctuation("?");
      }
    }

    return buf;
  }

  public record TypeArgument(KType type, ProtoBuf.Type.Argument.Projection typeProjection) {
    public TextBuffer stringify(int indent) {
      TextBuffer buf = new TextBuffer();
      switch (typeProjection) {
        case IN -> buf.appendKeyword("in").appendWhitespace(" ");
        case OUT -> buf.appendKeyword("out").appendWhitespace(" ");
        case STAR -> {
          buf.appendPunctuation('*');
          return buf;
        }
        case INV -> {
        }
      }
      buf.append(type.stringify(indent));
      return buf;
    }
  }
}
