package org.vineflower.kotlin.struct;

import kotlin.metadata.internal.metadata.ProtoBuf;
import kotlin.metadata.internal.metadata.deserialization.Flags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kotlin.metadata.StructKotlinMetadataAttribute;

import java.util.List;
import java.util.stream.Collectors;

public class KContract implements Flags {
  private static final String INVOCATION_KIND = "kotlin.contracts.InvocationKind";
  @NotNull
  public final List<KEffect> effects;

  private KContract(@NotNull List<KEffect> effects) {
    this.effects = effects;
  }

  public static KContract from(ProtoBuf.Contract proto, List<KParameter> params, StructKotlinMetadataAttribute ktData) {
    return new KContract(proto.getEffectList().stream().map(it -> KEffect.from(it, params, ktData)).collect(Collectors.toList()));
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

    static KEffect from(ProtoBuf.Effect proto, List<KParameter> params, StructKotlinMetadataAttribute ktData) {
      ProtoBuf.Effect.EffectType type = proto.hasEffectType() ? proto.getEffectType() : null;
      List<KExpression> expressions = proto.getEffectConstructorArgumentList().stream().map(it -> KExpression.from(it, params, ktData)).collect(Collectors.toList());
      KExpression conditionalConclusion = proto.hasConclusionOfConditionalEffect() ? KExpression.from(proto.getConclusionOfConditionalEffect(), params, ktData) : null;
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
          String name = func.valueParameterReference.name();
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

  public record KExpression(
    int flags,
    @Nullable KParameter valueParameterReference,
    @Nullable ProtoBuf.Expression.ConstantValue constantValue,
    @Nullable KType instanceofType,
    @NotNull List<KExpression> andArguments,
    @NotNull List<KExpression> orArguments
  ) {
    // Placeholder type for receiver type
    private static final KParameter THIS_TYPE = new KParameter(0, "this", KType.NOTHING, null, 0);

    static KExpression from(ProtoBuf.Expression proto, List<KParameter> params, StructKotlinMetadataAttribute ktData) {
      int flags = proto.getFlags();
      KParameter valueParameterReference = null;
      if (proto.hasValueParameterReference()) {
        int index = proto.getValueParameterReference();
        valueParameterReference = index == 0 ? THIS_TYPE : params.get(index - 1);
      }

      ProtoBuf.Expression.ConstantValue constantValue = proto.hasConstantValue() ? proto.getConstantValue() : null;
      KType instanceofType = null;
      if (proto.hasIsInstanceType()) {
        instanceofType = KType.from(proto.getIsInstanceType(), ktData.nameResolver);
      } else if (proto.hasIsInstanceTypeId()) {
        instanceofType = KType.from(proto.getIsInstanceTypeId(), ktData);
      }
      List<KExpression> andArguments = proto.getAndArgumentList().stream().map(it -> from(it, params, ktData)).collect(Collectors.toList());
      List<KExpression> orArguments = proto.getOrArgumentList().stream().map(it -> from(it, params, ktData)).collect(Collectors.toList());
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
        paramName = KotlinWriter.toValidKotlinIdentifier(valueParameterReference.name());
      }

      if (instanceofType != null) {
        buf.append(paramName)
          .append(' ')
          .append(IS_NEGATED.get(flags) ? "!is" : "is")
          .append(' ')
          .append(instanceofType.stringify(indent));
      } else if (IS_NULL_CHECK_PREDICATE.get(flags)) {
        buf.append(paramName)
          .append(' ')
          .append(IS_NEGATED.get(flags) ? "!=" : "==")
          .append(' ')
          .append("null");
      } else if (constantValue != null) {
        if (valueParameterReference != null && valueParameterReference.type().isNullable) {
          buf.append(paramName)
            .append(' ')
            .append(IS_NEGATED.get(flags) ? "!=" : "==")
            .append(' ')
            .append(constantValue.name().toLowerCase());
        } else {
          String output = valueParameterReference != null && "kotlin/Boolean".equals(valueParameterReference.type().kotlinType)
            ? paramName
            : constantValue.name().toLowerCase();

          if (IS_NEGATED.get(flags)) {
            buf.append('!');
          }

          buf.append(output);
        }
      } else if (valueParameterReference != null) {
        if (!valueParameterReference.type().kotlinType.equals("kotlin/Boolean")) {
          //TODO figure out why this happens
        }
        if (IS_NEGATED.get(flags)) {
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
