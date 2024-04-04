package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.vineflower.kotlin.util.ProtobufFlags;

public class KPropertyAccessor {
  public final ProtobufFlags.PropertyAccessor flags;
  public final MethodWrapper underlyingMethod;

  public KPropertyAccessor(ProtobufFlags.PropertyAccessor flags, MethodWrapper underlyingMethod) {
    this.flags = flags;
    this.underlyingMethod = underlyingMethod;
  }
}
