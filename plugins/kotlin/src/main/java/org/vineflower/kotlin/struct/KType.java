package org.vineflower.kotlin.struct;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinDecompilationContext;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.KTypes;

import java.util.Objects;

public class KType extends VarType {
  public static final KType UNIT = new KType(VARTYPE_VOID, "kotlin/Unit", false, null, null, null);
  public static final KType NOTHING = new KType(new VarType("java/lang/Void", true), "kotlin/Nothing", false, null, null, null);

  public final String kotlinType;
  public final boolean isNullable;
  public final TypeArgument @Nullable [] typeArguments;

  @Nullable
  public final String typeParameterName;

  @Nullable
  public final String typeAliasName;

  private KType(
    VarType type,
    String kotlinType,
    boolean isNullable,
    TypeArgument @Nullable [] typeArguments,
    @Nullable String typeParameterName,
    @Nullable String typeAliasName) {
    super(type.type, type.arrayDim, type.value, type.typeFamily, type.stackSize);
    this.kotlinType = kotlinType;
    this.isNullable = isNullable;
    this.typeArguments = typeArguments;
    this.typeParameterName = typeParameterName;
    this.typeAliasName = typeAliasName;
  }
  
  public static KType from(ProtoBuf.Type type, MetadataNameResolver nameResolver) {
    String kotlinType = type.hasClassName() ? nameResolver.resolve(type.getClassName()) : "kotlin/Any";
    boolean isNullable = type.getNullable();

    // short-circuit for `Unit`
    if ("kotlin/Unit".equals(kotlinType) && !isNullable) {
      return UNIT;
    }

    // similar short-circuit for `Nothing`
    if ("kotlin/Nothing".equals(kotlinType) && !isNullable) {
      return NOTHING;
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

    return new KType(varType, kotlinType, isNullable, typeArguments, typeParameterName, typeAliasName);
  }

  public static KType from(int tableIndex, MetadataNameResolver resolver) {
    ProtoBuf.TypeTable table;
    switch (KotlinDecompilationContext.getCurrentType()) {
      case CLASS:
        table = KotlinDecompilationContext.getCurrentClass().getTypeTable();
        break;
      case SYNTHETIC_CLASS:
        table = KotlinDecompilationContext.getSyntheticClass().getTypeTable();
        break;
      case FILE:
        table = KotlinDecompilationContext.getFilePackage().getTypeTable();
        break;
      case MULTIFILE_CLASS:
        table = KotlinDecompilationContext.getMultifilePackage().getTypeTable();
        break;
      default:
        throw new IllegalStateException("No decompilation context found");
    }

    return from(table.getType(tableIndex), resolver);
  }

  // stringify is for the decompiler output
  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();

    if (typeParameterName != null) {
      buf.append(typeParameterName);
      if (isNullable) {
        buf.append("?");
      }

      return buf;
    }

    if (!kotlinType.startsWith("kotlin/Function")) { // Non-functions are essentially equivalent to Java generic types
      String type = kotlinType.replace('/', '.');
      buf.append(DecompilerContext.getImportCollector().getShortName(type));

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
