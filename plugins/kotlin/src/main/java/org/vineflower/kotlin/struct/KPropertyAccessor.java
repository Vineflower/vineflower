package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.vineflower.kotlin.util.ProtobufFlags;

public record KPropertyAccessor(ProtobufFlags.PropertyAccessor flags, MethodWrapper underlyingMethod) {
}
