package org.vineflower.kotlin.struct;

import org.vineflower.kt.metadata.deserialization.Flags;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.token.TokenType;
import org.vineflower.kotlin.KotlinWriter;

public record KParameter(int flags, String name, KType type, KType varargType, int typeId) implements Flags {
  public void stringify(int indent, TextBuffer buf) {
    if (IS_CROSSINLINE.get(flags)) {
      buf.appendKeyword("crossinline").appendWhitespace(" ");
    }

    if (IS_NOINLINE.get(flags)) {
      buf.appendKeyword("noinline").appendWhitespace(" ");
    }

    // Vararg types are a bit odd to say the least
    boolean isVararg = varargType != null && type.kotlinType.equals("kotlin/Array");

    if (isVararg) {
      buf.appendKeyword("vararg").appendWhitespace(" ");
    }

    buf.append(KotlinWriter.toValidKotlinIdentifier(name), TokenType.PARAMETER).appendPunctuation(":").appendWhitespace(" ");

    KType type = isVararg ? varargType : this.type;
    buf.append(type.stringify(indent + 1));
  }
}
