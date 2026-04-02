package org.vineflower.kotlin.struct;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.token.TokenType;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kt.metadata.deserialization.Flags;

import java.util.List;
import java.util.stream.Collectors;

public class KContract implements Flags {
  private static final VarType INVOCATION_KIND = new VarType("kotlin/contracts/InvocationKind", true);
  @NotNull
  public final List<KEffect> effects;

  private KContract(@NotNull List<KEffect> effects) {
    this.effects = effects;
  }

  public static KContract from(ProtoBuf.Contract proto, List<KParameter> params, MetadataNameResolver resolver, ProtoBuf.TypeTable typeTable) {
    return new KContract(proto.getEffectList().stream().map(it -> KEffect.from(it, params, resolver, typeTable)).collect(Collectors.toList()));
  }

  public TextBuffer stringify(int indent) {
    TextBuffer buf = new TextBuffer();
    buf.appendIndent(indent)
      .appendMethod("contract", false, "kotlin/contracts/ContractBuilderKt", "contract", "(Lkotlin/jvm/functions/Function1;)V")
      .appendWhitespace(" ")
      .appendPunctuation("{")
      .appendLineSeparator();
    for (KEffect effect : effects) {
      effect.stringify(buf, indent + 1);
    }
    buf.appendIndent(indent).appendPunctuation("}").appendLineSeparator();
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

    static KEffect from(ProtoBuf.Effect proto, List<KParameter> params, MetadataNameResolver resolver, ProtoBuf.TypeTable typeTable) {
      ProtoBuf.Effect.EffectType type = proto.hasEffectType() ? proto.getEffectType() : null;
      List<KExpression> expressions = proto.getEffectConstructorArgumentList().stream().map(it -> KExpression.from(it, params, resolver, typeTable)).collect(Collectors.toList());
      KExpression conditionalConclusion = proto.hasConclusionOfConditionalEffect() ? KExpression.from(proto.getConclusionOfConditionalEffect(), params, resolver, typeTable) : null;
      ProtoBuf.Effect.InvocationKind kind = proto.hasKind() ? proto.getKind() : null;
      return new KEffect(type, expressions, conditionalConclusion, kind);
    }

    public void stringify(TextBuffer buf, int indent) {
      if (type == null) return;

      buf.appendIndent(indent);

      switch (type) {
        case RETURNS_NOT_NULL:
          buf.appendMethod("returnsNotNull", false, "kotlin/contracts/ContractBuilder", "returnsNotNull", "()Lkotlin/contracts/ReturnsNotNull;").appendPunctuation("()");
          if (conditionalConclusion != null) {
            buf.appendWhitespace(" ").appendMethod("implies", false, "kotlin/contracts/SimpleEffect", "implies", (String) null).appendWhitespace(" ").appendPunctuation("(");
            conditionalConclusion.stringify(buf, indent, false);
            buf.appendPunctuation(')');
          }
          break;
        case CALLS:
          buf.appendMethod("callsInPlace", false, "kotlin/contracts/ContractBuilder", "callsInPlace", "(Lkotlin/Function;Lkotlin/contracts/InvocationKind;)Lkotlin/contracts/CallsInPlace;").appendPunctuation("(");
          KExpression func = expressions.get(0);
          String name = func.valueParameterReference.name();
          buf.append(KotlinWriter.toValidKotlinIdentifier(name), TokenType.PARAMETER);
          if (kind != null) {
            buf.appendPunctuation(",").appendWhitespace(" ")
              .appendTypeName(INVOCATION_KIND)
              .appendPunctuation(".")
              .appendField(kind.name(), false, INVOCATION_KIND.value, kind.name(), INVOCATION_KIND.toString());
          }
          buf.appendPunctuation(")");
          break;
        case RETURNS_CONSTANT:
          buf.appendMethod("returns", false, "kotlin/contracts/ContractBuilder", "returns", "()Lkotlin/contracts/Returns;").appendPunctuation("(");
          if (!expressions.isEmpty()) {
            KExpression expr = expressions.get(0);
            expr.stringify(buf, indent, false);
          }
          buf.appendPunctuation(")");
          if (conditionalConclusion != null) {
            buf.appendWhitespace(" ").appendMethod("implies", false, "kotlin/contracts/SimpleEffect", "implies", "()Lkotlin/contracts/SimpleEffect;").appendWhitespace(" ").appendPunctuation("(");
            conditionalConclusion.stringify(buf, indent, false);
            buf.appendPunctuation(')');
          }
          break;
      }
      buf.appendLineSeparator();
    }
  }

  public record KExpression(
    int flags,
    int index,
    @Nullable KParameter valueParameterReference,
    @Nullable ProtoBuf.Expression.ConstantValue constantValue,
    @Nullable KType instanceofType,
    @NotNull List<KExpression> andArguments,
    @NotNull List<KExpression> orArguments
  ) {
    // Placeholder type for receiver type
    private static final KParameter THIS_TYPE = new KParameter(0, "this", KType.NOTHING, null, 0);

    static KExpression from(ProtoBuf.Expression proto, List<KParameter> params, MetadataNameResolver resolver, ProtoBuf.TypeTable typeTable) {
      int flags = proto.getFlags();
      KParameter valueParameterReference = null;
      int index = -1;
      if (proto.hasValueParameterReference()) {
        index = proto.getValueParameterReference();
        valueParameterReference = index == 0 ? THIS_TYPE : params.get(index - 1);
      }

      ProtoBuf.Expression.ConstantValue constantValue = proto.hasConstantValue() ? proto.getConstantValue() : null;
      KType instanceofType = null;
      if (proto.hasIsInstanceType()) {
        instanceofType = KType.from(proto.getIsInstanceType(), resolver);
      } else if (proto.hasIsInstanceTypeId()) {
        instanceofType = KType.from(proto.getIsInstanceTypeId(), resolver, typeTable);
      }
      List<KExpression> andArguments = proto.getAndArgumentList().stream().map(it -> from(it, params, resolver, typeTable)).collect(Collectors.toList());
      List<KExpression> orArguments = proto.getOrArgumentList().stream().map(it -> from(it, params, resolver, typeTable)).collect(Collectors.toList());
      return new KExpression(flags, index, valueParameterReference, constantValue, instanceofType, andArguments, orArguments);
    }

    public void stringify(TextBuffer buf, int indent, boolean partOfOr) {
      if (!andArguments.isEmpty() && (!orArguments.isEmpty() || partOfOr)) {
        // all `&&` predicates must be evaluated before any `||` predicates
        buf.appendPunctuation('(');
      }

      boolean appended = true;

      MethodWrapper method = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
      String paramName = null;
      if (valueParameterReference == THIS_TYPE) {
        paramName = "this";
      } else if (valueParameterReference != null) {
        paramName = KotlinWriter.toValidKotlinIdentifier(valueParameterReference.name());
      }

      if (instanceofType != null) {
        if (valueParameterReference == THIS_TYPE) {
          buf.appendKeyword("this");
        } else {
          buf.appendVariable(paramName, false, true, method.classStruct.qualifiedName, method.methodStruct.getName(), method.desc(), index, paramName);
        }
        buf.appendWhitespace(" ")
          .appendKeyword(IS_NEGATED.get(flags) ? "!is" : "is")
          .appendWhitespace(" ")
          .append(instanceofType.stringify(indent));
      } else if (IS_NULL_CHECK_PREDICATE.get(flags)) {
        if (valueParameterReference == THIS_TYPE) {
          buf.appendKeyword("this");
        } else {
          buf.appendVariable(paramName, false, true, method.classStruct.qualifiedName, method.methodStruct.getName(), method.desc(), index, paramName);
        }
        buf.appendWhitespace(" ")
          .appendOperator(IS_NEGATED.get(flags) ? "!=" : "==")
          .appendWhitespace(" ")
          .appendKeyword("null");
      } else if (constantValue != null) {
        if (valueParameterReference != null && valueParameterReference.type().isNullable) {
          if (valueParameterReference == THIS_TYPE) {
            buf.appendKeyword("this");
          } else {
            buf.appendVariable(paramName, false, true, method.classStruct.qualifiedName, method.methodStruct.getName(), method.desc(), index, paramName);
          }
          buf.appendWhitespace(" ")
            .appendOperator(IS_NEGATED.get(flags) ? "!=" : "==")
            .appendWhitespace(" ")
            .appendKeyword(constantValue.name().toLowerCase());
        } else {
          if (IS_NEGATED.get(flags)) {
            buf.appendOperator('!');
          }

          if (valueParameterReference != null && "kotlin/Boolean".equals(valueParameterReference.type().kotlinType)) {
            buf.appendVariable(paramName, false, true, method.classStruct.qualifiedName, method.methodStruct.getName(), method.desc(), index, paramName);
          } else {
            buf.appendKeyword(constantValue.name().toLowerCase());
          }
        }
      } else if (valueParameterReference != null) {
        if (!valueParameterReference.type().kotlinType.equals("kotlin/Boolean")) {
          //TODO figure out why this happens
        }
        if (IS_NEGATED.get(flags)) {
          buf.appendOperator('!');
        }
        buf.appendVariable(paramName, false, true, method.classStruct.qualifiedName, method.methodStruct.getName(), method.desc(), index, paramName);
      } else {
        appended = false;
      }

      if (!andArguments.isEmpty()) {
        for (KExpression andArgument : andArguments) {
          if (appended) {
            buf.appendWhitespace(" ").appendOperator("&&").appendWhitespace(" ");
          }
          appended = true;

          andArgument.stringify(buf, indent, false);
        }
      }

      if (!orArguments.isEmpty()) {
        if (!andArguments.isEmpty() || partOfOr) {
          buf.appendPunctuation(')');
        }
        for (KExpression orArgument : orArguments) {
          if (appended) {
            buf.appendWhitespace(" ").appendOperator("||").appendWhitespace(" ");
          }
          appended = true;

          orArgument.stringify(buf, indent, true);
        }
      }
    }
  }
}
