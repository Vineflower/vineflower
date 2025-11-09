package org.vineflower.kotlin.struct;

import kotlin.metadata.internal.metadata.deserialization.Flags;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinWriter;

public record KParameter(int flags, String name, KType type, KType varargType, int typeId) implements Flags {
  public void stringify(int indent, TextBuffer buf) {
    if (IS_CROSSINLINE.get(flags)) {
      buf.append("crossinline ");
    }

    if (IS_NOINLINE.get(flags)) {
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
