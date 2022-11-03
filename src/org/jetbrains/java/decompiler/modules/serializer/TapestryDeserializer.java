package org.jetbrains.java.decompiler.modules.serializer;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;

@FunctionalInterface
public interface TapestryDeserializer {
  Exprent deserialize(ExprParser.Arg arg);
}
