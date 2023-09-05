package org.vineflower.kotlin.struct;

import org.vineflower.kotlin.util.ProtobufFlags;

public class KParameter {
  public final ProtobufFlags.ValueParameter flags;
  public final String name;
  public final KType type;
  public final KType varargType;
  public final int typeId;

  KParameter(ProtobufFlags.ValueParameter flags, String name, KType type, KType varargType, int typeId) {
    this.flags = flags;
    this.name = name;
    this.type = type;
    this.varargType = varargType;
    this.typeId = typeId;
  }
}
