package org.quiltmc.quiltflower.kotlin.struct;

import org.jetbrains.java.decompiler.struct.StructMethod;
import org.quiltmc.quiltflower.kotlin.util.ProtobufFlags;

public class KPropertyAccessor {
  public final ProtobufFlags.PropertyAccessor flags;
  public final StructMethod underlyingMethod;

  public KPropertyAccessor(ProtobufFlags.PropertyAccessor flags, StructMethod underlyingMethod) {
    this.flags = flags;
    this.underlyingMethod = underlyingMethod;
  }
}
