package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.util.ProtobufFlags;

public record KParameter(ProtobufFlags.ValueParameter flags, String name, KType type, KType varargType, int typeId) {
  public void stringify(int indent, TextBuffer buf) {
    if (flags.isCrossinline) {
      buf.append("crossinline ");
    }

    if (flags.isNoinline) {
      buf.append("noinline ");
    }

    // Vararg types are a bit odd to say the least
    boolean isVararg = varargType != null && type.kotlinType.equals("kotlin/Array");

    if (isVararg) {
      buf.append("vararg ");
    }

    buf.append(KotlinWriter.toValidKotlinIdentifier(name)).append(": ");

    KType type = isVararg ? varargType : this.type;
    buf.append(type.stringify(indent + 1));
  }
}
