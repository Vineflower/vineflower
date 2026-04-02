package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.struct.StructMethod;

import java.util.function.Function;

public record KPropertyAccessor(int flags, Function<ClassWrapper, MethodWrapper> methodSupplier, StructMethod methodStruct) {
}
