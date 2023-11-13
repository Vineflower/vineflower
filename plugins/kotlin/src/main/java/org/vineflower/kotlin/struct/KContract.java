package org.vineflower.kotlin.struct;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.util.ProtobufFlags;

import java.util.List;
import java.util.stream.Collectors;

public class KContract {
  private static final String INVOCATION_KIND = "kotlin.contracts.InvocationKind";
  @NotNull
  public final List<KEffect> effects;

  private KContract(@NotNull List<KEffect> effects) {
    this.effects = effects;
  }

  public static KContract from(ProtoBuf.Contract proto, List<KParameter> params, MetadataNameResolver nameResolver) {
    return new KContract(proto.getEffectList().stream().map(it -> KEffect.from(it, params, nameResolver)).collect(Collectors.toList()));
  }

  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();
    buf.appendIndent(indent).append("contract {").appendLineSeparator();
    for (KEffect effect : effects) {
      effect.stringify(buf, indent + 1);
    }
    buf.appendIndent(indent).append("}").appendLineSeparator();
    return buf;
  }

  public static class KEffect {
    @Nullable
    public final ProtoBuf.Effect.EffectType type;
    @NotNull
    public final List<KExpression> expressions;
    @Nullable
    public final KExpression conditionalConclusion;
    @Nullable
    public final ProtoBuf.Effect.InvocationKind kind;

    private KEffect(
      @Nullable ProtoBuf.Effect.EffectType type,
      @NotNull List<KExpression> expressions,
      @Nullable KExpression conditionalConclusion,
      @Nullable ProtoBuf.Effect.InvocationKind kind) {
      this.expressions = expressions;
      this.type = type;
      this.conditionalConclusion = conditionalConclusion;
      this.kind = kind;
    }

    static KEffect from(ProtoBuf.Effect proto, List<KParameter> params, MetadataNameResolver nameResolver) {
      ProtoBuf.Effect.EffectType type = proto.hasEffectType() ? proto.getEffectType() : null;
      List<KExpression> expressions = proto.getEffectConstructorArgumentList().stream().map(it -> KExpression.from(it, params, nameResolver)).collect(Collectors.toList());
      KExpression conditionalConclusion = proto.hasConclusionOfConditionalEffect() ? KExpression.from(proto.getConclusionOfConditionalEffect(), params, nameResolver) : null;
      ProtoBuf.Effect.InvocationKind kind = proto.hasKind() ? proto.getKind() : null;
      return new KEffect(type, expressions, conditionalConclusion, kind);
    }

    public void stringify(TextBuffer buf, int indent) {
      if (type == null) return;

      buf.appendIndent(indent);

      switch (type) {
        case RETURNS_NOT_NULL:
          buf.append("returnsNotNull()");
          if (conditionalConclusion != null) {
            buf.append(" implies (");
            conditionalConclusion.stringify(buf, indent, false);
            buf.append(')');
          }
          break;
        case CALLS:
          buf.append("callsInPlace(");
          KExpression func = expressions.get(0);
          String name = func.valueParameterReference.name;
          buf.append(KotlinWriter.toValidKotlinIdentifier(name));
          if (kind != null) {
            buf.append(", ")
              .append(DecompilerContext.getImportCollector().getShortName(INVOCATION_KIND))
              .append(".")
              .append(kind.name());
          }
          buf.append(")");
          break;
        case RETURNS_CONSTANT:
          buf.append("returns(");
          if (!expressions.isEmpty()) {
            KExpression expr = expressions.get(0);
            expr.stringify(buf, indent, false);
          }
          buf.append(")");
          if (conditionalConclusion != null) {
            buf.append(" implies (");
            conditionalConclusion.stringify(buf, indent, false);
            buf.append(')');
          }
          break;
      }
      buf.appendLineSeparator();
    }
  }

  public static class KExpression {
    // Placeholder type for receiver type
    private static final KParameter THIS_TYPE = new KParameter(new ProtobufFlags.ValueParameter(0), "this", KType.NOTHING, null, 0);

    @NotNull
    public final ProtobufFlags.Expression flags;
    @Nullable
    public final KParameter valueParameterReference;
    @Nullable
    public final ProtoBuf.Expression.ConstantValue constantValue;
    @Nullable
    public final KType instanceofType; // isInstanceType
    @NotNull
    public final List<KExpression> andArguments;
    @NotNull
    public final List<KExpression> orArguments;

    private KExpression(
      @NotNull ProtobufFlags.Expression flags,
      @Nullable KParameter valueParameterReference,
      @Nullable ProtoBuf.Expression.ConstantValue constantValue,
      @Nullable KType instanceofType,
      @NotNull List<KExpression> andArguments,
      @NotNull List<KExpression> orArguments) {
      this.flags = flags;
      this.valueParameterReference = valueParameterReference;
      this.constantValue = constantValue;
      this.instanceofType = instanceofType;
      this.andArguments = andArguments;
      this.orArguments = orArguments;
    }

    static KExpression from(ProtoBuf.Expression proto, List<KParameter> params, MetadataNameResolver nameResolver) {
      ProtobufFlags.Expression flags = new ProtobufFlags.Expression(proto.getFlags());
      KParameter valueParameterReference = null;
      if (proto.hasValueParameterReference()) {
        int index = proto.getValueParameterReference();
        valueParameterReference = index == 0 ? THIS_TYPE : params.get(index - 1);
      }

      ProtoBuf.Expression.ConstantValue constantValue = proto.hasConstantValue() ? proto.getConstantValue() : null;
      KType instanceofType = null;
      if (proto.hasIsInstanceType()) {
        instanceofType = KType.from(proto.getIsInstanceType(), nameResolver);
      } else if (proto.hasIsInstanceTypeId()) {
        instanceofType = KType.from(proto.getIsInstanceTypeId(), nameResolver);
      }
      List<KExpression> andArguments = proto.getAndArgumentList().stream().map(it -> from(it, params, nameResolver)).collect(Collectors.toList());
      List<KExpression> orArguments = proto.getOrArgumentList().stream().map(it -> from(it, params, nameResolver)).collect(Collectors.toList());
      return new KExpression(flags, valueParameterReference, constantValue, instanceofType, andArguments, orArguments);
    }

    public void stringify(TextBuffer buf, int indent, boolean partOfOr) {
      if (!andArguments.isEmpty() && (!orArguments.isEmpty() || partOfOr)) {
        // all `&&` predicates must be evaluated before any `||` predicates
        buf.append('(');
      }

      boolean appended = true;

      String paramName = null;
      if (valueParameterReference == THIS_TYPE) {
        paramName = "this";
      } else if (valueParameterReference != null) {
        paramName = KotlinWriter.toValidKotlinIdentifier(valueParameterReference.name);
      }

      if (instanceofType != null) {
        buf.append(paramName)
          .append(' ')
          .append(flags.isNegated ? "!is" : "is")
          .append(' ')
          .append(instanceofType.stringify(indent));
      } else if (flags.isNullPredicate) {
        buf.append(paramName)
          .append(' ')
          .append(flags.isNegated ? "!=" : "==")
          .append(' ')
          .append("null");
      } else if (constantValue != null) {
        if (valueParameterReference != null && valueParameterReference.type.isNullable) {
          buf.append(paramName)
            .append(' ')
            .append(flags.isNegated ? "!=" : "==")
            .append(' ')
            .append(constantValue.name().toLowerCase());
        } else {
          String output = valueParameterReference != null && "kotlin/Boolean".equals(valueParameterReference.type.kotlinType)
            ? paramName
            : constantValue.name().toLowerCase();

          if (flags.isNegated) {
            buf.append('!');
          }

          buf.append(output);
        }
      } else if (valueParameterReference != null) {
        if (!valueParameterReference.type.kotlinType.equals("kotlin/Boolean")) {
          //TODO figure out why this happens
        }
        if (flags.isNegated) {
          buf.append('!');
        }
        buf.append(paramName);
      } else {
        appended = false;
      }

      if (!andArguments.isEmpty()) {
        for (KExpression andArgument : andArguments) {
          if (appended) {
            buf.append(" && ");
          }
          appended = true;

          andArgument.stringify(buf, indent, false);
        }
      }

      if (!orArguments.isEmpty()) {
        if (!andArguments.isEmpty() || partOfOr) {
          buf.append(')');
        }
        for (KExpression orArgument : orArguments) {
          if (appended) {
              buf.append(" || ");
          }
          appended = true;

          orArgument.stringify(buf, indent, true);
        }
      }
    }
  }
}
