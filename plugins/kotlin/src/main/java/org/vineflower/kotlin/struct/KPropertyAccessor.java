package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.main.rels.MethodWrapper;

public record KPropertyAccessor(int flags, MethodWrapper underlyingMethod) {
}
